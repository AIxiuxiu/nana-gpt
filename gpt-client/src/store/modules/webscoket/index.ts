import { getWsUrl } from '@/utils';
import mitt from 'mitt';
import { defineStore } from 'pinia';
import { useUserStore } from '../user';

const WSURL = getWsUrl();

export const useWSStore = defineStore('webScoket', {
  state: () => ({
    websocket: null,
    webSocketError: 0,
    webSocketErrorMax: 5,
    emitter: mitt()
  }),
  getters: {
    user() {
      const useStore = useUserStore();
      return useStore;
    }
  },
  actions: {
    sendMessage(data) {
      if (this.websocket) {
        this.websocket.send(JSON.stringify(data));
      }
    },
    connectWs() {
      if (!this.user.isLogin) {
        return;
      }

      const wsUrl = `${WSURL}/message/${this.user.id}?token=${this.user.token}`;
      try {
        if (this.websocket === null) {
          this.websocket = new WebSocket(wsUrl); // WebSocket服务端地址
          console.log('等待服务器Websocket握手包');
        } else {
          return;
        }
        this.websocket.onopen = (event) => {
          console.log('收到服务器Websocket握手包', event);
          try {
            this.webSocketError = 0;
          } catch (error) {
            console.error('收到信息格式错误', event.data);
          }
        };
        this.websocket.onmessage = (event) => {
          try {
            const resData = JSON.parse(event.data);
            this.handleMessage(resData);
          } catch (error) {
            console.error('处理信息错误', error);
          }
        };
        this.websocket.onclose = () => {
          console.log('和服务器断开连接');
          this.websocket = null;
          if (this.webSocketError < this.webSocketErrorMax) {
            setTimeout(() => {
              this.connectWs();
            }, 5000);
          } else {
            console.warn('已达到最大重连次数');
          }
        };
        this.websocket.onerror = () => {
          this.websocket = null;
          this.webSocketError++;
        };
      } catch (error) {
        console.error('ws的地址错误！');
      }
      window.onbeforeunload = () => {
        this.closeWs();
      };
    },
    closeWs() {
      if (this.websocket) {
        this.webSocketError = 100;
        this.websocket.close();
      }
    },
    handleMessage(data) {
      console.log(data);
      if (data.type == 'error' && data.flag) {
        ElMessage.error(data.message);
      } else if (data.flag) {
        this.emitter.emit(data.type, data);
      }
    }
  }
});
