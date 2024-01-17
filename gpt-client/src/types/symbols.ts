/**
 * Inject 类型说明
 */

import { InjectionKey } from 'vue';
import { IQjInjectTable } from './types';

/**
 * 刷新
 */
export const InjectReloadKey: InjectionKey<Function> = Symbol('Reload');

/**
 * 表格 Table
 */
export const InjectTabkeKey: (id?: string) => InjectionKey<IQjInjectTable> = (id?: string) => {
  return Symbol.for('Table' + (id ? id : ''));
};
