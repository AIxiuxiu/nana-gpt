import dayjs from 'dayjs';

const getDates = (days) => {
  const end = dayjs();
  const start = dayjs().subtract(days, 'day');
  return [start, end];
};

/**
 * dateRange 相关重复代码
 * rerurn { shortcuts:快捷选项 }
 */
export const useDateRange = function () {
  const shortcuts = [
    // {
    //   text: '今日',
    //   value: getDates(0)
    // },
    {
      text: '最近一周',
      value: getDates(7)
    },
    {
      text: '最近一月',
      value: getDates(30)
    },
    {
      text: '最近三个月',
      value: getDates(90)
    }
  ];
  return { shortcuts };
};
