import { ref } from 'vue';
/**
 * 使用倒计时
 * @param time 时间
 * @returns
 */
export const useCountDown = function (time = 60, initText = '发送', repeatText = `重新发送`) {
  const count = ref(0);
  const countText = ref(initText);
  let timer;
  const start = () => {
    count.value = time;
    if (!timer) {
      countText.value = `${count.value} s`;
      timer = setInterval(() => {
        count.value--;
        countText.value = `${count.value} s`;
        if (count.value === 0) {
          clearInterval(timer);
          timer = null;
          countText.value = repeatText;
        }
      }, 1000);
    }
  };

  return { start, count, countText };
};
