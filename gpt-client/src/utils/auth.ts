import { storage } from './storage';

const TokenKey = 'token';
const TokenPrefix = '';
const UserKey = 'user';

const getToken = () => {
  return storage.get<string>(TokenKey);
};
const setToken = (token: string) => {
  storage.set(TokenKey, token);
};
const clearToken = () => {
  storage.remove(TokenKey);
};

function isLogin() {
  const user = storage.get<any>(UserKey);
  if (!user) {
    logout();
    return false;
  }
  if (!!getToken() && user.token && user.token.length > 0 && user.expireTime && user.expireTime > new Date().getTime() / 1000) {
    return true;
  } else {
    logout();
    return false;
  }
}

function logout() {
  clearToken();
  storage.remove(UserKey);
}

export { TokenPrefix, isLogin, getToken, setToken, clearToken };
