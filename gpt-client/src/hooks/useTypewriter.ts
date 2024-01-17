// 打字机队列

export default function (onConsume: (str: string, tokens: number) => void) {
  let queue: string[] = [];
  let consuming = false;
  let timmer: any;
  let tokens: number;

  function dynamicSpeed() {
    const speed = 2000 / queue.length;
    if (speed > 200) {
      return 200;
    } else {
      return speed;
    }
  }

  // 添加字符串到队列
  function add(str: string) {
    if (!str) return;
    queue.push(...str.split(''));
    tokens += 1;
  }
  // 消费
  function consume() {
    if (queue.length > 0) {
      const str = queue.shift();
      str && onConsume(str, 0);
    }
  }
  // 消费下一个
  function next() {
    consume();
    // 根据队列中字符的数量来设置消耗每一帧的速度，用定时器消耗
    timmer = setTimeout(() => {
      consume();
      if (consuming) {
        next();
      }
    }, dynamicSpeed());
  }
  // 开始消费队列
  function start() {
    consuming = true;
    tokens = 0;
    next();
  }
  // 结束消费队列
  function done() {
    consuming = false;
    clearTimeout(timmer);
    // 把queue中剩下的字符一次性消费
    onConsume(queue.join(''), tokens);
    queue = [];
    tokens = 0;
  }

  return { start, next, done, add };
}
