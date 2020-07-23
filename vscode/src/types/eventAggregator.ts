export interface IEventAggregator {
    subscribe<TArgs>(event: string, callback: (args: TArgs) => void): void;
    publish<TArgs>(event: String, args: TArgs): void;
}

export class EventAggregator implements IEventAggregator {

    private _subscriber = new  Map<String, ((args: any) => void)[]>();

    public subscribe<TArgs>(event: string, callback: (args: TArgs) => void): void {
        if (!this._subscriber.has(event)) { this._subscriber.set(event, []); }
        
        const callbacks = this._subscriber.get(event);
        callbacks?.push(callback);
    }

    public publish<TArgs>(event: String, args: TArgs): void {
        if (!this._subscriber.has(event)) { return; }

        const callbacks = this._subscriber.get(event);
        callbacks?.forEach(callback => {
            callback(args);
        });
    }
}