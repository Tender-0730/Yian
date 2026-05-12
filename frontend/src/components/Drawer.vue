<script setup>
import { ref } from "vue";
import { defineProps } from "vue";
import { defineExpose } from "vue";
import { defineEmits } from "vue";
const emit = defineEmits(["formSubmit", "formCancel"]);

const props = defineProps(["title"]);
let showDrawer = ref(false); //默认隐藏抽屉

//打开/关闭抽屉
const open = () => {
  showDrawer.value = true;
};

const close = () => {
  showDrawer.value = false;
};

const cancelClick = () => {
  emit("formCancel");
};

//确认
const confirmClick = () => {
  emit("formSubmit");
};

//将open()和close()暴露给父组件
defineExpose({
  open,
  close,
});
</script>

<template>
  <el-drawer v-model="showDrawer" direction="rtl">
    <div>
      <h4>{{ title }}</h4>
    </div>
    <!-- 插槽：用于放置表单内容 -->
    <div>
      <slot name="Form"></slot>
    </div>

    <div style="flex: auto">
      <el-button @click="cancelClick">取消</el-button>
      <el-button type="primary" @click="confirmClick">确认</el-button>
    </div>
  </el-drawer>
</template>

<style scoped></style>
