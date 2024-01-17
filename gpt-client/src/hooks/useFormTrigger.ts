import { formItemContextKey } from 'element-plus';
import { inject } from 'vue';

//  触发el-form-item的校验事件 trigger
export function useFormTrigger() {
  const elFormItem: any = inject(formItemContextKey);
  const emitTrigger = (value: any) => {
    if (elFormItem) {
      elFormItem.validate('blur');
      elFormItem.validate('change');
    }
  };
  return { elFormItem, emitTrigger };
}
