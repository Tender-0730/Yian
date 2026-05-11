<script setup>
import axios from "axios";
import { onMounted, ref } from "vue";
import PageContainer from "@/components/PageContainer.vue";
import Drawer from "@/components/Drawer.vue";
import { Plus, Delete, Edit } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { formatTime } from "@/utils/format.js";

// 获取数据
const MessageList = ref([]);
const getMessageList = async () => {
  const res = await axios.get("/api/MessageList");
  // 处理数据，拼接楼层和房间号
  MessageList.value = (res.data || []).map((item) => {
    try {
      const roomStr = String(item.RoomNumber || "");
      // 使用正则表达式检查房间号格式是否正确（4位数字）
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
let pageSize = ref(4); //一页显示几个
let currentPage = ref(1); //当前展示的页码
let showList = ref([]); //当前展示的列表
const getPaging = () => {
  total.value = MessageList.value.length;
  showList.value = MessageList.value.slice(
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
  await getMessageList();
  getPaging();
});
//新增/编辑
let drawerRef = ref(null);
//表单数据对象
const formData = ref({
  name: "",
  gender: "",
  age: "",
  phone: "",
  RoomNumber: "",
  LevelOfcare: "",
  picture: "",
  createTime: "", // 添加创建时间字段
});
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
  phone: [
    { required: true, message: "联系电话不为空", trigger: "submit" },
    {
      pattern: /^(1[3-9]\d{9}|(?:0\d{2,3}-?)?[1-9]\d{6,7})$/,
      message: "请输入正确的电话号码格式",
      trigger: "blur",
    },
  ],
});
//无论是新增还是编辑：打开drawer，只是传递的值不一样
//val是新增还是修改的标识,add edit
let mode = ref(""); //接收新增还是编辑的标识
let mark = ref(null); //标识符 判断
const openDrawer = async (val, id = null) => {
  mode.value = val;
  mark.value = id;
  //判断是新增还是编辑
  console.log(mark.value);
  if (val === "add") {
    //新增
    formData.value = {
      name: "",
      temperature: "",
      Blood: "",
      heartRate: "",
      status: "已入住",
      executor: "",
      date: new Date().toISOString(),
      abnormal: "",
      picture:
        "https://pic4.zhimg.com/v2-bfabd4c4834ef8cf2212e09f5758f00c_r.jpg?source=1940ef5c",
    };
  } else {
    //编辑==>通过id去获取信息
    formData.value = (await axios.get(`/api/MessageList/${id}`)).data;
  }
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
  formRef.value.validate(async (res) => {
    if (!res) return;

    if (mode.value === "add") {
      const now = new Date().toISOString();
      const submitData = {
        ...formData.value,
        createTime: now, // 使用createTime作为创建时间
      };
      console.log("提交的数据：", submitData); // 调试日志

      await axios.post("/api/MessageList", submitData);
      await getMessageList();
      getPaging();
      ElMessage.success("添加成功");
    } else {
      await axios.put(`/api/MessageList/${mark.value}`, formData.value);
      await getMessageList();
      getPaging();
      ElMessage.success("编辑成功");
    }
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
  await getMessageList();
  getPaging();
  ElMessage.success("删除成功");
};

// 序号计算方法
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1;
};
</script>

<template>
  <PageContainer title="信息管理">
    <template #extra>
      <el-button @click="openDrawer('add')" type="primary">添加</el-button>
    </template>
    <el-table :data="showList" style="width: 100%">
      <el-table-column
        label="序号"
        width="80"
        type="index"
        :index="indexMethod"
      />
      <el-table-column label="姓名" prop="name" />
      <el-table-column label="性别" prop="gender" />
      <el-table-column label="年龄" prop="age" />
      <el-table-column label="住址信息" prop="RoomNumber" />
      <el-table-column label="照片" v-slot="pic">
        <img :src="pic.row.picture" width="80px" />
      </el-table-column>
      <el-table-column label="入院时间" prop="date">
        <template #default="{ row }">
          {{ formatTime(row.date) }}
        </template>
      </el-table-column>
      <el-table-column label="护理级别" v-slot="rank">
        <el-dropdown placement="bottom-start">
          <el-button> {{ rank.row.LevelOfcare }} </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>普通护理</el-dropdown-item>
              <el-dropdown-item>中级护理</el-dropdown-item>
              <el-dropdown-item>特级护理</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-table-column>
      <el-table-column align="right">
        <!-- 搜索 -->
        <template #header>
          <el-input
            v-model="search"
            size="small"
            placeholder="请输入查询名字"
            @change="doSearch"
          />
        </template>
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
        <!-- 表单组件 -->
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
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="formData.phone"></el-input>
          </el-form-item>
          <el-form-item label="住址信息" prop="RoomNumber">
            <el-input
              maxlength="4"
              minlength="4"
              placeholder="请输入四位数数字"
              v-model="formData.RoomNumber"
            />
          </el-form-item>
          <el-form-item label="护理级别" prop="LevelOfcare">
            <el-select
              v-model="formData.LevelOfcare"
              placeholder="请选择护理级别"
            >
              <el-option label="基础护理" value="基础护理"></el-option>
              <el-option label="中级护理" value="中级护理"></el-option>
              <el-option label="特级护理" value="特级护理"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="照片">
            <el-upload
              class="avatar-uploader"
              action=""
              :show-file-list="false"
            >
              <img
                class="avatar"
                v-if="mode === 'edit'"
                :src="formData.picture"
              />
              <el-icon class="avatar-uploader-icon" v-else>
                <Plus />
              </el-icon>
            </el-upload>
          </el-form-item>
        </el-form>
      </template>
    </Drawer>
  </PageContainer>
</template>

<style lang="scss" scoped>
.avatar {
  width: 200px;
  height: 200px;
  object-fit: cover;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
