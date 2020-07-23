import {Disposable, window, QuickInputButtons, QuickInputButton, QuickInput, QuickPickItem} from 'vscode';

export interface WizardStep<T> {
	execute(context: WizardContext<T>): Promise<void>;
	canExecute(context: WizardContext<T>): boolean;
}

export abstract class WizardStepBase<T> implements WizardStep<T> {
	abstract execute(context: WizardContext<T>): Promise<void>;
	abstract canExecute(context: WizardContext<T>): boolean;

	protected notEmpty(input: string): Promise<string|undefined> {
	  return Promise.resolve(input ? undefined : 'Value is obligatory');
	}

	protected optional(input: string): Promise<string|undefined> {
	  return Promise.resolve(undefined);
	}
}

export interface WizardContext<T> {
	target: T;
	showInputBox<P extends InputBoxParameters>({value, prompt, validate, buttons, placeholder, password}: P): Promise<string | (P extends { buttons: (infer I)[] } ? I : never)>;
	showQuickPick<T extends QuickPickItem, P extends QuickPickParameters<T>>({items, activeItem, placeholder, buttons}: P): Promise<T | (P extends { buttons: (infer I)[] } ? I : never)>;
}

export interface InputBoxParameters {
	value: string;
	prompt: string;
	placeholder: string | undefined;
	password: boolean;
	validate: (value: string) => Promise<string | undefined>;
	buttons?: QuickInputButton[];
}

export interface QuickPickParameters<T extends QuickPickItem> {
	items: T[];
	activeItem?: T;
	placeholder: string;
	buttons?: QuickInputButton[];
}
// TODO: based on
class InputFlowAction {
  private constructor() { }
	static back = new InputFlowAction();
	static cancel = new InputFlowAction();
	static resume = new InputFlowAction();
}

export class Wizard<T> implements WizardContext<T> {
    private _aborted = false;
    private _stepPointer = 0;
	private _current?: QuickInput;
	private _executed: boolean[];

	public get aborted(): boolean {
	  return this._aborted;
	}

	private get _totalSteps(): number {
	  let c = this._currentStep - 1;
	  for (let i = this._stepPointer; i < this._steps.length; i ++) {
	    if (this._executed[i] || this._steps[i].canExecute(this)) {
	      c ++;
	    }
	  }

	  return c;
	}

	private get _currentStep(): number {
	  let c = 1;
	  for (let i = 0; i < this._stepPointer; i ++) {
	    c += this._executed[i] ? 1 : 0;
	  }

	  return c;
	}

	public constructor(
		public target: T,
		private _title: string,
        private _steps: WizardStep<T>[],
	) {
	  this._executed = this._steps.map((step) => false);
	}

	public async execute(): Promise<T> {
	  for (;this._stepPointer < this._steps.length; this._stepPointer ++) {
	    try {
	      const step = this._steps[this._stepPointer];
	      if (this._executed[this._stepPointer] || step.canExecute(this)) {
	        await step.execute(this);
	        this._executed[this._stepPointer] = true;
	      }
	    } catch (err) {
	      if (err === InputFlowAction.back) {
	        this._executed[this._stepPointer] = false;
	        this._stepPointer = this._executed.lastIndexOf(true) - 1;
	      } else if (err === InputFlowAction.resume) {
	        this._stepPointer --;
	      } else if (err === InputFlowAction.cancel) {
	        this._aborted = true;
	        break;
	      } else {
	        throw err;
	      }
	    }
	  }

	  if (this._current) {
	    this._current.dispose();
	  }

	  return this.target;
	}

	async showInputBox<P extends InputBoxParameters>({value, prompt, validate, buttons, placeholder, password}: P) {
	  const disposables: Disposable[] = [];
	  try {
	    return await new Promise<string |(P extends { buttons: (infer I)[] } ? I : never)>((resolve, reject) => {
	      const input = window.createInputBox();
	      input.title = this._title;
	      input.step = this._currentStep;
	      input.totalSteps = this._totalSteps;
	      input.value = value || '';
	      input.prompt = prompt;
	      input.placeholder = placeholder;
	      input.password = password;
	      input.buttons = [
	        ...(this._currentStep > 1 ? [QuickInputButtons.Back] : []),
	        ...(buttons || []),
	      ];
	      let validating = validate('');
	      disposables.push(
	          input.onDidTriggerButton((item) => {
	            if (item === QuickInputButtons.Back) {
	              reject(InputFlowAction.back);
	            } else {
	              resolve(<any>item);
	            }
	          }),
	          input.onDidAccept(async () => {
	            const value = input.value;
	            input.enabled = false;
	            input.busy = true;
	            if (!(await validate(value))) {
	              resolve(value);
	            }
	            input.enabled = true;
	            input.busy = false;
	          }),
	          input.onDidChangeValue(async (text) => {
	            const current = validate(text);
	            validating = current;
	            const validationMessage = await current;
	            if (current === validating) {
	              input.validationMessage = validationMessage;
	            }
	          }),
	          input.onDidHide(() => {
	            (async () => {
	              reject(this._shouldResume && await this._shouldResume() ? InputFlowAction.resume : InputFlowAction.cancel);
	            })()
	                .catch(reject);
	          }),
	      );
	      if (this._current) {
	        this._current.dispose();
	      }
	      this._current = input;
	      this._current.show();
	    });
	  } finally {
	    disposables.forEach((d) => d.dispose());
	  }
	}

	async showQuickPick<T extends QuickPickItem, P extends QuickPickParameters<T>>({items, activeItem, placeholder, buttons}: P) {
	  const disposables: Disposable[] = [];
	  try {
	    return await new Promise<T |(P extends { buttons: (infer I)[] } ? I : never)>((resolve, reject) => {
	      const input = window.createQuickPick<T>();
	      input.title = this._title;
	      input.step = this._currentStep;
	      input.totalSteps = this._totalSteps;
	      input.placeholder = placeholder;
	      input.items = items;
	      if (activeItem) {
	        input.activeItems = [activeItem];
	      }
	      input.buttons = [
	        ...(this._currentStep > 1 ? [QuickInputButtons.Back] : []),
	        ...(buttons || []),
	      ];
	      disposables.push(
	          input.onDidTriggerButton((item) => {
	            if (item === QuickInputButtons.Back) {
	              reject(InputFlowAction.back);
	            } else {
	              resolve(<any>item);
	            }
	          }),
	          input.onDidChangeSelection((items) => resolve(items[0])),
	          input.onDidHide(() => {
	            (async () => {
	              reject(this._shouldResume && await this._shouldResume() ? InputFlowAction.resume : InputFlowAction.cancel);
	            })()
	                .catch(reject);
	          }),
	      );
	      if (this._current) {
	        this._current.dispose();
	      }
	      this._current = input;
	      this._current.show();
	    });
	  } finally {
	    disposables.forEach((d) => d.dispose());
	  }
	}

	private _shouldResume() {
	  return window.showInformationMessage(`Wizard '${this._title}' has been closed. Resume?`, 'Yes', 'No')
	      .then((option) => {
	        if (option === 'Yes') {
	          return true;
	        } else {
	          return false;
	        }
	      });
	}
}
