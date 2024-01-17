// 是否合法IP地址
export function validateIP(_rule, value, callback) {
  if (value == '' || value == undefined || value == null) {
    callback();
  } else {
    const reg = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
    if (!reg.test(value) && value != '') {
      callback(new Error('请输入正确的IP地址'));
    } else {
      callback();
    }
  }
}

// 是否合法统一社会信用代码
export function validateCompanyNo(_rule, value, callback) {
  if (value == '' || value == undefined || value == null) {
    callback();
  } else {
    const reg = /([0-9A-Za-z]{15})|([0-9A-Za-z]{18})|([0-9A-Za-z]{20})/;
    if (!reg.test(value) && value != '') {
      callback(new Error('请输入正确的统一社会信用代码'));
    } else {
      callback();
    }
  }
}

// 是否手机号码或者固话
export function validatePhoneTwo(_rule, value, callback) {
  const reg = /^((0\d{2,3}-\d{7,8})|(1[3456789]\d{9}))$/;
  if (value == '' || value == undefined || value == null) {
    callback();
  } else {
    if (!reg.test(value) && value != '') {
      callback(new Error('请输入正确的电话号码或者固话号码'));
    } else {
      callback();
    }
  }
}

// 是否固话
export function validateTelphone(_rule, value, callback) {
  const reg = /0\d{2,3}-\d{7,8}/;
  if (value == '' || value == undefined || value == null) {
    callback();
  } else {
    if (!reg.test(value) && value != '') {
      callback(new Error('请输入正确的固定电话'));
    } else {
      callback();
    }
  }
}

// 是否手机号码
export function validatePhone(_rule, value, callback) {
  const reg = /^1[3456789]\d{9}$/;
  if (value == '' || value == undefined || value == null) {
    callback();
  } else {
    if (!reg.test(value) && value != '') {
      callback(new Error('请输入正确的电话号码'));
    } else {
      callback();
    }
  }
}

// 是否身份证号码
export function validateIdNo(_rule, value, callback) {
  const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
  if (value == '' || value == undefined || value == null) {
    callback();
  } else {
    if (!reg.test(value) && value != '') {
      callback(new Error('请输入正确的身份证号码'));
    } else {
      callback();
    }
  }
}

// 是否邮箱
export function validateEMail(_rule, value, callback) {
  const reg = /^([a-zA-Z0-9]+[-_\.]?)+@[a-zA-Z0-9]+(\.[a-zA-Z0-9]+){1,}$/;
  if (value == '' || value == undefined || value == null) {
    callback();
  } else {
    if (!reg.test(value)) {
      callback(new Error('请输入正确的邮箱'));
    } else {
      callback();
    }
  }
}

// 合法网站地址
export function validateWebURL(_rule, value, callback) {
  if (value == '' || value == undefined || value == null) {
    callback();
  } else {
    if (!validateURL(value)) {
      callback(new Error('请输入正确的网址'));
    } else {
      callback();
    }
  }
}

// 合法url
export function validateURL(url) {
  const urlregex = /^http(s?):\/\/.+/;
  return urlregex.test(url);
}

// 验证内容是否包含英文数字以及下划线
export function isPassword(_rule, value, callback) {
  const reg = /^[_a-zA-Z0-9]+$/;
  if (value == '' || value == undefined || value == null) {
    callback();
  } else {
    if (!reg.test(value)) {
      callback(new Error('仅由英文字母，数字以及下划线组成'));
    } else {
      callback();
    }
  }
}

// 自动检验数值的范围
export function checkMax20000(_rule, value, callback) {
  if (value == '' || value == undefined || value == null) {
    callback();
  } else if (!Number(value)) {
    callback(new Error('请输入[1,20000]之间的数字'));
  } else if (value < 1 || value > 20000) {
    callback(new Error('请输入[1,20000]之间的数字'));
  } else {
    callback();
  }
}

// 验证是否1-99之间
export function isOneToNinetyNine(_rule, value, callback) {
  if (!value) {
    return callback(new Error('输入不可以为空'));
  }
  setTimeout(() => {
    if (!Number(value)) {
      callback(new Error('请输入正整数'));
    } else {
      const re = /^[1-9][0-9]{0,1}$/;
      const rsCheck = re.test(value);
      if (!rsCheck) {
        callback(new Error('请输入正整数，值为【1,99】'));
      } else {
        callback();
      }
    }
  }, 0);
}

// 验证是否整数
export function isInteger(_rule, value, callback) {
  if (!value) {
    return callback(new Error('输入不可以为空'));
  }
  setTimeout(() => {
    if (!Number(value)) {
      callback(new Error('请输入正整数'));
    } else {
      const re = /^[0-9]*[1-9][0-9]*$/;
      const rsCheck = re.test(value);
      if (!rsCheck) {
        callback(new Error('请输入正整数'));
      } else {
        callback();
      }
    }
  }, 0);
}

// 验证是否整数,非必填
export function isIntegerNotMust(_rule, value, callback) {
  if (!value) {
    callback();
  }
  setTimeout(() => {
    if (!Number(value)) {
      callback(new Error('请输入正整数'));
    } else {
      const re = /^[0-9]*[1-9][0-9]*$/;
      const rsCheck = re.test(value);
      if (!rsCheck) {
        callback(new Error('请输入正整数'));
      } else {
        callback();
      }
    }
  }, 1000);
}

//  验证是否是[0-1]的小数
export function isDecimal(_rule, value, callback) {
  if (!value) {
    return callback(new Error('输入不可以为空'));
  }
  setTimeout(() => {
    if (!Number(value)) {
      callback(new Error('请输入[0,1]之间的数字'));
    } else {
      if (value < 0 || value > 1) {
        callback(new Error('请输入[0,1]之间的数字'));
      } else {
        callback();
      }
    }
  }, 100);
}

//  验证是否是[1-10]的小数,即不可以等于0
export function isBtnOneToTen(_rule, value, callback) {
  if (typeof value == 'undefined') {
    return callback(new Error('输入不可以为空'));
  }
  setTimeout(() => {
    if (!Number(value)) {
      callback(new Error('请输入正整数，值为[1,10]'));
    } else {
      if (!(value == '1' || value == '2' || value == '3' || value == '4' || value == '5' || value == '6' || value == '7' || value == '8' || value == '9' || value == '10')) {
        callback(new Error('请输入正整数，值为[1,10]'));
      } else {
        callback();
      }
    }
  }, 100);
}

// 验证是否是[1-100]的小数,即不可以等于0
export function isBtnOneToHundred(_rule, value, callback) {
  if (!value) {
    return callback(new Error('输入不可以为空'));
  }
  setTimeout(() => {
    if (!Number(value)) {
      callback(new Error('请输入整数，值为[1,100]'));
    } else {
      if (value < 1 || value > 100) {
        callback(new Error('请输入整数，值为[1,100]'));
      } else {
        callback();
      }
    }
  }, 100);
}

// 验证是否是[0-100]的小数
export function isBtnZeroToHundred(_rule, value, callback) {
  if (!value) {
    return callback(new Error('输入不可以为空'));
  }
  setTimeout(() => {
    if (!Number(value)) {
      callback(new Error('请输入[1,100]之间的数字'));
    } else {
      if (value < 0 || value > 100) {
        callback(new Error('请输入[1,100]之间的数字'));
      } else {
        callback();
      }
    }
  }, 100);
}

// 验证端口是否在[0,65535]之间
export function isPort(_rule, value, callback) {
  if (!value) {
    return callback(new Error('输入不可以为空'));
  }
  setTimeout(() => {
    if (value == '' || typeof value == undefined) {
      callback(new Error('请输入端口值'));
    } else {
      const re = /^([0-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|6[0-4]\d{3}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/;
      const rsCheck = re.test(value);
      if (!rsCheck) {
        callback(new Error('请输入在[0-65535]之间的端口值'));
      } else {
        callback();
      }
    }
  }, 100);
}

// 验证端口是否在[0,65535]之间，非必填,isMust表示是否必填
export function isCheckPort(_rule, value, callback) {
  if (!value) {
    callback();
  }
  setTimeout(() => {
    if (value == '' || typeof value == undefined) {
      //callback(new Error('请输入端口值'));}else {
      const re = /^([0-9]|[1-9]\d|[1-9]\d{2}|[1-9]\d{3}|[1-5]\d{4}|6[0-4]\d{3}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/;
      const rsCheck = re.test(value);
      if (!rsCheck) {
        callback(new Error('请输入在[0-65535]之间的端口值'));
      } else {
        callback();
      }
    }
  }, 100);
}

// 两位小数验证
export const validateValidity = (_rule, value, callback) => {
  if (!/(^[1-9]([0-9]+)?(\.[0-9]{1,2})?$)|(^(0){1}$)|(^[0-9]\.[0-9]([0-9])?$)/.test(value)) {
    callback(new Error('最多两位小数！！！'));
  } else {
    callback();
  }
};

// 密码校验
export const validatePsdReg = (_rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入密码'));
  }
  if (!/^(?![\d]+$)(?![a-zA-Z]+$)(?![^\da-zA-Z]+$)([^\u4e00-\u9fa5\s]){6,18}$/.test(value)) {
    callback(new Error('请输入6-20位英文字母、数字或者符号（除空格），且字母、数字和标点符号至少包含两种'));
  } else {
    callback();
  }
};

export const validatePassword = (_rule, value, callback) => {
  if (/^[a-zA-Z\d]{6,16}$/.test(value)) {
    callback();
  } else {
    callback(new Error('请填写正确的密码'));
  }
};

// 中文校验
export const validateContacts = (_rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入中文'));
  }
  if (!/^[\u0391-\uFFE5A-Za-z]+$/.test(value)) {
    callback(new Error('不可输入特殊字符'));
  } else {
    callback();
  }
};

//  账号校验
export const validateCode = (_rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入账号'));
  }
  if (!/^(?![0-9]*$)(?![a-zA-Z]*$)[a-zA-Z0-9]{6,20}$/.test(value)) {
    callback(new Error('账号必须为6-20位字母和数字组合'));
  } else {
    callback();
  }
};

//纯数字校验
export const validateNumber = (_rule, value, callback) => {
  const numberReg = /^\d+$|^\d+[.]?\d+$/;
  if (value !== '') {
    if (!numberReg.test(value)) {
      callback(new Error('请输入数字'));
    } else {
      callback();
    }
  } else {
    callback(new Error('请输入值'));
  }
};

// 最多一位小数
export const onePoint = (_rule, value, callback) => {
  if (!/^[0-9]+([.]{1}[0-9]{1})?$/.test(value)) {
    callback(new Error('最多一位小数！！！'));
  } else {
    callback();
  }
};

// 小写字母
export function validateLowerCase(val) {
  const reg = /^[a-z]+$/;
  return reg.test(val);
}

/* 大写字母*/
export function validateUpperCase(str) {
  const reg = /^[A-Z]+$/;
  return reg.test(str);
}

/* 大小写字母*/
export function validatAlphabets(str) {
  const reg = /^[A-Za-z]+$/;
  return reg.test(str);
}

/* 是否手机号码*/
export function validatePhoneNum(_rule, value, callback) {
  const reg = /^[1][3,4,5,7,8,9][0-9]{9}$/;
  if (value == '' || value == undefined || value == null) {
    callback();
  } else {
    if (!reg.test(value) && value != '') {
      callback(new Error('请输入正确的手机号码'));
    } else {
      callback();
    }
  }
}

// 是编码
export function validateSerialNumber(_rule, value, callback) {
  const reg = /^[1-9]([0-9])*(\.[0-9]+)*$/;
  if (value == '' || value == undefined || value == null) {
    callback();
  } else {
    if (!reg.test(value)) {
      callback(new Error('请输入正确的序号，仅支持数字和点'));
    } else {
      callback();
    }
  }
}
