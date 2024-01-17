export type RoleType = '' | '*' | 'admin' | 'user';
export interface UserState {
  id?: string;
  username?: string;
  token?: string;
  expireTime?: number;
}
