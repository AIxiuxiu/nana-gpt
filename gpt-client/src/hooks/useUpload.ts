import { AxiosRequestConfig } from 'axios';
import { upload } from '../utils/http';

export enum QjFilePath {
  default = '', //默认
  public = 'public', //公共目录
  user = 'user' //用户信息
}

/**
 * 上传文件
 * @param fileType
 * @param file
 * @param callback
 * @returns
 */
export const useUpload = (filePath: QjFilePath, file: File, progressCallback = new Function()): Promise<{ id: string; name: string; url: string; type?: string }> => {
  return new Promise((resolve, reject) => {
    if (!file) {
      reject('文件不能为空！');
      return;
    }

    const formData = new FormData();
    formData.append('bucketName', filePath);
    formData.append('files', file);
    formData.append('preview', 'perm');

    const config: AxiosRequestConfig = {
      onUploadProgress: function (progressEvent) {
        if (progressEvent.upload) {
          const percent = Number((progressEvent.progress * 100).toFixed(0));
          progressCallback({ percent: percent });
        }
      }
    };

    upload<any>('/file/upload', formData, config)
      .then((data: any[]) => {
        if (data && data.length > 0) {
          const file = data[0];
          if (!file.error) {
            resolve({ id: file.patchName, name: file.fileName, type: file.fileType, url: file.url });
          } else {
            reject('上传文件失败' + file.error);
          }
        } else {
          reject('上传文件失败');
        }
      })
      .catch((err) => {
        console.error('上传文件失败！' + err);
        reject('上传文件失败！');
      });
  });
};

/**
 * 判断是不是图片
 * @param file
 * @returns
 */
export const useIsImg = (file: File) => {
  return file.type === 'image/jpeg' || 'image/jpg' || 'image/gif' || 'image/png';
};

/**
 * 图片大小判断
 * @param file 图片
 * @param size 大小
 * @returns
 */
export const useImgSize = (file: File, size: { width: number; height: number }): Promise<{ width: number; height: number }> => {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = function () {
      const imgSize = { width: img.width, height: img.height };
      const status = img.width === size.width && img.height === size.height;
      status ? resolve(imgSize) : reject(imgSize);
    };
    img.src = window.URL.createObjectURL(file);
  });
};
