/*
 * @Description: vue相关声明
 * @Author: ahl
 * @Date: 2021-09-29 10:43:20
 * @LastEditTime: 2023-01-18 15:05:55
 */

import dayjs from 'dayjs';

declare module '@vue/runtime-core' {
  export interface ComponentCustomProperties {
    $dayjs: typeof dayjs;
    $img: (name: string) => string;
    $const: { roadshowCover: string };
    $imgPreview: { open: (imgs: string | string[], opt?: { zIndex?: number; initialIndex?: boolean; infinite?: boolean; hideOnClickModal?: boolean }) => void; close: () => void };
  }
}
