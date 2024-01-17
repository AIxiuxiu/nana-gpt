/*
 * @name: checkNum
 * @param: intNum表示整数位个数，默认0为不限制
 * @param: decNum表示小数位个数，默认0为没小数
 * @param: maxLength用于限制输入的数字位数（包含小数位）
 * @param: max用于限制输入的数字最大值
 * @param: min用于限制输入的数字最小值
 */
export const checkNum = (obj: any, intNum = 0, decNum = 0, maxLength = 0, max = 0, min = 0) => {
  let value = obj.value;
  let changeValue: string, t1: string, t2: string;
  if (decNum === 0) {
    value = value.replace(/[^\d]/g, ''); //  去除数字以外的字符，若要支持负数，可将此处正则改为/[^\-\d]/g
    value = value.replace(/^0\d+/g, '0'); // 防止整数位出现'00'的情况

    obj.value = value;
    if (intNum !== 0) {
      value = value.substr(0, intNum);
    }
  } else {
    // value = value.replace(/[^\d.]/g, '') // 去除数字和小数点以外的字符，若要支持负数，可将此处正则改为/[^\-\d.]/g
    value = value.replace(/[^\-\d.]/g, ''); // 去除数字和小数点以外的字符，若要支持负数，可将此处正则改为/[^\-\d.]/g
    // value = value.replace(/^[^\d]/g, '') // 保证第一个字符是数字，若要支持负数，可将此处正则改为/^[^\-\d]/g
    value = value.replace(/^[^\-\d]/g, ''); // 保证第一个字符是数字，若要支持负数，可将此处正则改为/^[^\-\d]/g
    value = value.replace(/\.{2}/g, '.'); // 去除第二个小数点
    value = value.replace(/^0\d+/g, '0');
    changeValue = value.split('.');
    if (changeValue.length > 1) {
      //  表示用户输入的既有整数又有小数
      if (intNum === 0) {
        t1 = changeValue[0];
      } else {
        t1 = changeValue[0].slice(0, intNum);
      }
      t2 = changeValue[1].slice(0, decNum);
      value = t1 + '.' + t2;
    } else {
      if (intNum !== 0) {
        value = value.substr(0, intNum);
      }
    }
  }
  if (maxLength !== 0) {
    value = value.slice(0, maxLength);
  }
  if (max !== 0 && value > max) {
    value = max;
  }
  if (min !== 0 && value < min) {
    value = min;
  }
  if (obj.value !== value) {
    obj.value = value;
  }
  return value;
};

export const useInputNumber = () => {
  /*
   * @name: validateNum 校验el-input仅填写数字、负数、小数
   * @param: val表示当前输入的值
   * @param: row表示表单当前行数据（根据自己实际情况修改）
   * @param: field表示表单字段名
   * @param: intNum表示整数位个数，默认0为不限制
   * @param: decNum表示小数位个数，默认0为没小数
   * @param: maxLength用于限制输入的数字位数，默认0（包含小数位）
   * @param: max用于限制输入的数字最大值
   * @param: min用于限制输入的数字最小值
   */
  const validateNum = (val: any, row: any, field: string, intNum = 0, decNum = 0, maxLength = 0, max = 0, min = 0) => {
    const value = checkNum({ value: val }, intNum, decNum, maxLength, max, min);
    row[field] = value; // 注意：这里的值可能是字符串也可能是数字
  };

  return validateNum;
};
