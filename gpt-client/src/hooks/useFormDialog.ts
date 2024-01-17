import { ref } from 'vue';

/**
 * 简单表单弹窗
 */
export default function useFormDialog(forms: any, emits: any) {
  const formsRef = ref(null);

  // 关闭弹窗
  function handleClose() {
    formsRef.value.resetFields();
    emits('update:modelValue', false);
  }

  // 确定
  function handleConfirm() {
    formsRef.value.validate((valid) => {
      if (valid) {
        emits('confirm', Object.assign({}, forms));
        formsRef.value.resetFields();
        handleClose();
      } else {
        return false;
      }
    });
  }

  return { emits, formsRef, handleClose, handleConfirm };
}
