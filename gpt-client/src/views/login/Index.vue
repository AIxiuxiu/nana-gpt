<template>
  <div class="content">
    <div class="content-inner">
      <el-form ref="loginFormRef" size="large" :model="userFormData" class="login-form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="userFormData.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item prop="password" style="margin-top: 36px">
          <el-input v-model="userFormData.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item style="margin-top: 36px">
          <el-button :loading="logining" type="primary" @click="handleSubmit()">登 录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<route lang="yaml">
meta:
  layout: index
  hideNav: true
</route>

<script setup lang="ts">
/**
 * 登录页面
 */
import { useUserStore } from '@/store';
import { FormInstance, FormRules } from 'element-plus';

const router = useRouter();
const route = useRoute();

const userStore = useUserStore();
const userFormData = reactive({
  username: '',
  password: ''
});
const loginFormRef = ref<FormInstance>();
const rules: FormRules = {
  username: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
};

const redirectUrl = route.query.redirectUrl as string;
const logining = ref(false);
const handleSubmit = () => {
  if (!loginFormRef.value) return;
  loginFormRef.value.validate((valid) => {
    if (valid) {
      logining.value = true;
      userStore
        .login(userFormData)
        .then(() => {
          // 登录后进个人中心页面
          router.push(redirectUrl ? redirectUrl : '/chatView');
        })
        .finally(() => {
          logining.value = false;
        });
    } else {
      return false;
    }
  });
};
</script>

<style lang="scss" scoped>
.content {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
.content-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 360px;
  width: 460px;
  padding: 0 60px;
  margin-bottom: 8%;
  background: rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  backdrop-filter: blur(4.5px);
  -webkit-backdrop-filter: blur(4.5px);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.login-form {
  padding-top: 10px;
  width: 360px;
}

:deep(.el-button) {
  width: 100%;
  height: 48px;
  font-size: 20px;
  border-radius: 8px;
  background-color: #278cf0;
  background-image: linear-gradient(121deg, #278cf0 0%, #5eb8f5 100%);
  border: 1px solid rgba(255, 255, 255, 0.18);
}

:deep(.el-input__wrapper) {
  font-size: 18px;
  --el-input-height: 48px;
  --el-input-bg-color: rgba(255, 255, 255, 0.6);
  // --el-input-border: none;
  // --el-input-focus-border-color: transparent;
}
</style>
