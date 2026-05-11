<script setup>
import PageContainer from "@/components/PageContainer.vue";
import axios from "axios";
import { onMounted, ref } from "vue";
import { formatTime } from "@/utils/format.js";
import { Edit, Delete } from "@element-plus/icons-vue";
import Drawer from "@/components/Drawer.vue";
import { ElMessage } from "element-plus";

const nurseList = ref([]);
const loading = ref(false); // loading状态
const getNurseList = async () => {
  loading.value = true;
  const res = await axios.get("/api/nurseList");
  nurseList.value = res.data;
  // console.log(nurseList.value);
  loading.value = false;
};
const total = ref(0); //总条数
let pageSize = ref(7); //一页显示几个
let currentPage = ref(1); //当前展示的页码
let showList = ref([]); //当前展示的列表
const getPaging = () => {
  total.value = nurseList.value.length;
  showList.value = nurseList.value.slice(
    (currentPage.value - 1) * pageSize.value,
    currentPage.value * pageSize.value
  );
};
//单击页码跳转对应的页面  val:单击的页码数
const handlePage = (val) => {
  currentPage.value = val;
  getPaging();
};
//新增/编辑
let drawerRef = ref(null);
let nurseDate = ref({});
//表单数据校验
const formRules = ref({
  name: [{ required: true, message: "姓名不能为空", trigger: "submit" }],
  status: [{ required: true, message: "状态不能为空", trigger: "submit" }],
  executor: [
    { required: true, message: "护理人员不能为空", trigger: "submit" },
  ],
});
//无论是新增还是编辑：打开drawer，只是传递的值不一样
//val是新增还是修改的标识,add edit
let mode = ref("");
let mark = ref(null);
const openDrawer = async (val, id = null) => {
  mode.value = val;
  mark.value = id;
  //判断是新增还是编辑
  // console.log(mark.value, mode.value);
  if (val === "add") {
    //新增
    nurseDate.value = {
      name: "",
      temperature: "",
      Blood: "",
      heartRate: "",
      status: "",
      executor: "",
      date: Date(),
      abnormal: "",
    };
  } else {
    //编辑==>通过id去获取信息
    nurseDate.value = (await axios.get(`/api/nurseList/${id}`)).data;
  }
  //打开抽屉
  drawerRef.value.open();
};
// 提交
const formRef = ref(null);
const formSubmit = () => {
  formRef.value.validate(async (res) => {
    if (!res) return;
    //验证通过
    if (mode.value === "add") {
      await axios.post("/api/nurseList", nurseDate.value);
      await getNurseList();
      getPaging();
      ElMessage.success("添加成功");
    } else {
      await axios.put(`/api/nurseList/${mark.value}`, nurseDate.value);
      await getNurseList();
      getPaging();
      ElMessage.success("编辑成功");
    }
    //关闭抽屉
    drawerRef.value.close();
  });
};
// 取消
const formCancel = () => {
  nurseDate.value = {};
  drawerRef.value.close();
};
// 搜索
// const name = ref("");
const state = ref("");
const onSearch = async () => {
  const res = await axios.get("/api/nurseList");
  showList.value = res.data.filter((item) => item.status === state.value);
  getNurseList();
};
// 重置
const onReset = () => {
  state.value = "";
};
// 删除
const onDelete = async (id) => {
  await axios.delete(`/api/nurseList/${id}`);
  getNurseList();
  getPaging();
};
// 添加序号计算方法
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1;
};
onMounted(async () => {
  await getNurseList();
  getPaging();
});
</script>

<template>
  <page-container title="护理管理">
    <template #extra>
      <el-button type="primary" @click="openDrawer('add')">添加</el-button>
    </template>

    <!-- 表单区域 -->
    <el-form inline>
      <!-- <el-form-item label="姓名:">
        <el-input v-model="name" placeholder="请输入姓名"></el-input>
      </el-form-item> -->
      <el-form-item label="任务状态:">
        <!-- 这里后台标记发布状态，就是通过中文标记的，已发布 / 草稿 -->
        <el-select v-model="state" placeholder="请选择" style="width: 200px">
          <el-option label="已完成" value="已完成"></el-option>
          <el-option label="未完成" value="未完成"></el-option>
        </el-select>
        <!-- <el-input></el-input> -->
      </el-form-item>
      <el-form-item>
        <el-button @click="onSearch" type="primary">搜索</el-button>
        <el-button @click="onReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格区域 -->
    <el-table :data="showList" v-loading="loading">
      <el-table-column
        label="序号"
        width="80"
        type="index"
        :index="indexMethod"
      />
      <el-table-column label="姓名" prop="name"></el-table-column>
      <el-table-column label="体温(C°)" prop="temperature"></el-table-column>
      <el-table-column label="血压(mmHg)" prop="Blood"></el-table-column>
      <el-table-column label="心率(次/分钟)" prop="heartRate"></el-table-column>
      <el-table-column label="记录时间" prop="date">
        <template #default="{ row }">
          {{ formatTime(row.date) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status"></el-table-column>
      <el-table-column label="异常情况" prop="abnormal"></el-table-column>
      <el-table-column label="护理人员" prop="executor"></el-table-column>
      <!-- 利用作用域插槽 row 可以获取当前行的数据 => v-for 遍历 item -->
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button
            circle
            plain
            type="primary"
            :icon="Edit"
            @click="openDrawer('edit', row.id)"
          ></el-button>
          <el-button
            circle
            plain
            type="danger"
            :icon="Delete"
            @click="onDelete(row.id)"
          ></el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        @current-change="handlePage"
      />
    </div>
    <Drawer
      :title="mark ? '编辑' : '添加'"
      ref="drawerRef"
      @formSubmit="formSubmit"
      @formCancel="formCancel"
    >
      <template #Form>
        <!-- 表单组件 -->
        <el-form :model="nurseDate" :rules="formRules" ref="formRef">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="nurseDate.name" />
          </el-form-item>
          <el-form-item label="体温" prop="temperature">
            <el-input v-model="nurseDate.temperature"></el-input>
          </el-form-item>
          <el-form-item label="血压" prop="Blood">
            <el-input v-model="nurseDate.Blood"></el-input>
          </el-form-item>
          <el-form-item label="心率" prop="heartRate">
            <el-input v-model="nurseDate.heartRate"></el-input>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="nurseDate.status" placeholder="请选择状态">
              <el-option label="已完成" value="已完成"></el-option>
              <el-option label="未完成" value="未完成"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="异常情况" prop="abnormal">
            <el-input v-model="nurseDate.abnormal"></el-input>
          </el-form-item>
          <el-form-item label="护理人员" prop="executor">
            <el-select
              placeholder="请选择护理人员"
              v-model="nurseDate.executor"
            >
              <el-option label="小美" value="小美"></el-option>
              <el-option label="小帅" value="小帅"></el-option>
              <el-option label="小婷" value="小婷"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </template>
    </Drawer>
  </page-container>
</template>

<style scoped>
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
