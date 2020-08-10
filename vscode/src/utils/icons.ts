import {ExtensionContext, Color, window} from 'vscode';

export namespace icons {
  /**
   * Gets the icon that indicates Saros support of an user.
   *
   * @export
   * @param {ExtensionContext} context The context of the extension
   * @return {string} Absolute path to the icon
   */
  export const getSarosSupportIcon = (context: ExtensionContext) => {
    return context.asAbsolutePath('/media/obj16/contact_saros_obj.png');
  };

  /**
   * Gets the icon that indicates that an user is online.
   *
   * @export
   * @param {ExtensionContext} context The context of the extension
   * @return {string} Absolute path to the icon
   */
  export const getIsOnlineIcon = (context: ExtensionContext) => {
    return context.asAbsolutePath('/media/obj16/contact_obj.png');
  };

  /**
   * Gets the icon that indicates that an user is offline.
   *
   * @export
   * @param {ExtensionContext} context The context of the extension
   * @return {string} Absolute path to the icon
   */
  export const getIsOfflinetIcon = (context: ExtensionContext) => {
    return context.asAbsolutePath('/media/obj16/contact_offline_obj.png');
  };

  /**
   * Gets the icon that indicates the ability to add an account.
   *
   * @export
   * @param {ExtensionContext} context The context of the extension
   * @return {string} Absolute path to the icon
   */
  export const getAddAccountIcon = (context: ExtensionContext) => {
    return context.asAbsolutePath('/media/btn/addaccount.png');
  };

  /**
   * Gets the icon that represents the annotation color of the user.
   * If icon doesn't exist it'll be created.
   *
   * @export
   * @param {ExtensionContext} context The context of the extension
   * @param {string} color The annotation color as hex representation
   * @return {string} Absolute path to the icon
   */
  export const getUserColorIcon = async (context: ExtensionContext, color: string) => {   
    const fs = require('fs');
    const fileName = `color_${color}_25x25.png`;
    const relativeFolderPath = '/media/colors-generated';
    const relativeFilePath = `${relativeFolderPath}/${fileName}`;
    const absoluteFilePath = context.asAbsolutePath(relativeFilePath);
    const absoluteFolderPath = context.asAbsolutePath(relativeFolderPath);
    if(!fs.existsSync(absoluteFilePath)) {
      if(!fs.existsSync(absoluteFolderPath)) {
        fs.mkdirSync(absoluteFolderPath);
      }
      try {
        await createColorImage(color, absoluteFilePath);
      } catch (error) {
        window.showErrorMessage(error);
      }
    }
    return absoluteFilePath;
  }

  /**
   * Creates an image filled with the given color.
   *
   * @param {string} color The annotation color as hex representation
   * @param {string} file Absolute path to the file
   * @return {Promise<void>} Awaitable promise that returns once the
   *  file has been created
   */
  function createColorImage(color: string, file: string): Promise<void> {
    const fs = require('fs');
    const pImage = require('pureimage');
    const canvas = pImage.make(25, 25);
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = color;
    ctx.fillRect(0, 0, 25, 25);
    return pImage.encodePNGToStream(canvas, fs.createWriteStream(file));
  }
}
