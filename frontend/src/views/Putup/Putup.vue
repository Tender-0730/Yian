<script setup>
import PageContainer from "@/components/PageContainer.vue";
import axios from "axios";
import { onMounted, ref } from "vue";
import { Edit, Delete } from "@element-plus/icons-vue";
import Drawer from "@/components/Drawer.vue";
import { ElMessage } from "element-plus";

const putupList = ref([]);
const getPutupList = async () => {
  const res = await axios.get("/api/MessageList");
  putupList.value = (res.data || []).map((item) => {
    try {
      if (item.status === "已退住") {
        return {
          ...item,
          RoomNumber: "已退住",
        };
      }

      const roomStr = String(item.RoomNumber || "");
      const isValidFormat = /^\d{4}$/.test(roomStr);

      let formattedRoom = "未知房间";
      if (isValidFormat) {
        const [building, floor, room, bed] = roomStr.split("");
        formattedRoom = `${building}栋-${floor}层-${room}房-${bed}床`;
      }

      return {
        ...item,
        RoomNumber: formattedRoom,
      };
    } catch (error) {
      console.error("房间号处理错误:", error, item);
      return {
        ...item,
        RoomNumber: "数据错误",
      };
    }
  });
};
//分页
let total = ref(0); //总条目数
let pageSize = ref(6); //一页显示几个
let currentPage = ref(1); //当前展示的页码
let showList = ref([]); //当前展示的列表
const getPaging = () => {
  total.value = putupList.value.length;
  showList.value = putupList.value.slice(
    (currentPage.value - 1) * pageSize.value,
    currentPage.value * pageSize.value
  );
};
//单击页码跳转对应的页面  val:单击的页码数
const handlePage = (val) => {
  currentPage.value = val;
  getPaging();
};
onMounted(async () => {
  await getPutupList();
  getPaging();
});
//新增/编辑
let drawerRef = ref(null);
//表单数据对象
let formData = ref({});
//表单数据校验
const formRules = ref({
  name: [{ required: true, message: "商品名称不为空", trigger: "submit" }],
  gender: [{ required: true, message: "商品类型不为空", trigger: "submit" }],
  RoomNumber: [
    { required: true, message: "商品价格不为空", trigger: "submit" },
  ],
  LevelOfcare: [
    { required: true, message: "商品介绍不为空", trigger: "submit" },
  ],
  age: [{ required: true, message: "商品介绍不为空", trigger: "submit" }],
  status: [{ required: true, message: "商品介绍不为空", trigger: "submit" }],
  phone: [
    { required: true, message: "商品介绍不为空", trigger: "submit" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "密码必须是 6-15位 的非空字符",
      trigger: "submit",
    },
  ],
});
let mark = ref(null); //id
const openDrawer = async (val, id = null) => {
  mark.value = id;
  //编辑==>通过id去获取信息
  formData.value = (await axios.get(`/api/MessageList/${id}`)).data;
  // }
  //打开抽屉
  drawerRef.value.open();
};
// 搜索
const search = ref("");
const doSearch = async () => {
  const res = await axios.get("/api/MessageList");
  MessageList.value = res.data.filter((item) =>
    item.name.includes(search.value)
  );
  search.value = "";
  getPaging();
};
// 提交
const formRef = ref(null);
const formSubmit = () => {
  //验证数据是否合法 :通过返回true ，不通过返回false
  formRef.value.validate(async (res) => {
    if (!res) return;
    await axios.put(`/api/MessageList/${mark.value}`, formData.value);
    await getPutupList();
    getPaging();
    ElMessage.success("编辑成功");
    //关闭抽屉
    drawerRef.value.close();
  });
};
// 取消
const formCancel = () => {
  formData.value = {};
  drawerRef.value.close();
};
// 删除
const toDelete = async (id) => {
  await axios.delete(`/api/MessageList/${id}`);
  await getPutupList();
  getPaging();
  ElMessage.success("删除成功");
};
onMounted(() => {
  getPutupList();
  getPaging();
});

// 序号计算方法
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1;
};
</script>

<template>
  <PageContainer title="住宿管理">
    <!-- <template #extra>
      <el-button @click="openDrawer('add')" type="primary">添加</el-button>
    </template> -->
    <el-table :data="showList" style="width: 100%">
      <el-table-column
        label="序号"
        width="80"
        type="index"
        :index="indexMethod"
      />
      <el-table-column label="姓名" prop="name" />
      <el-table-column label="床位名称" prop="RoomNumber" />
      <el-table-column label="性别" prop="gender" />
      <el-table-column label="年龄" prop="age" />
      <el-table-column label="电话号码" prop="phone" />
      <el-table-column label="入住状态" prop="status">
        <template #default="{ row }">
          <span
            :class="[
              'status-tag',
              row.status === '已入住' ? 'status-in' : 'status-out',
            ]"
          >
            {{ row.status }}
          </span>
        </template>
      </el-table-column>
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
            @click="toDelete(row.id)"
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
    <!-- 抽屉 -->
    <Drawer
      :title="mark ? '编辑' : '添加'"
      ref="drawerRef"
      @formSubmit="formSubmit"
      @formCancel="formCancel"
    >
      <template #Form>
        <el-form :model="formData" :rules="formRules" ref="formRef">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="formData.name" />
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-select v-model="formData.gender" placeholder="请选择性别">
              <el-option label="男" value="男"></el-option>
              <el-option label="女" value="女"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="年龄" prop="age">
            <el-input v-model.number="formData.age" />
          </el-form-item>
          <el-form-item label="房间号" prop="RoomNumber">
            <el-input v-model.number="formData.RoomNumber" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model.number="formData.phone" />
          </el-form-item>
          <el-form-item label="入住状态" prop="status">
            <el-select v-model="formData.status" placeholder="请选择入住状态">
              <el-option label="已入住" value="已入住"></el-option>
              <el-option label="已退住" value="已退住"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </template>
    </Drawer>
  </PageContainer>
</template>

<style scoped>
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

<style lang="scss" scoped>
.status-tag {
  display: inline-block;
  padding: 2px 12px;
  border-radius: 10px; // 更圆润的边角
  font-size: 14px;

  &.status-in {
    background-color: #67c23a;
    color: #fff;
  }

  &.status-out {
    background-color: #909399;
    color: #fff;
  }
}
</style>
