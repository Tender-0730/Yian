<script setup>
import PageContainer from "@/components/PageContainer.vue";
import { onMounted, ref } from "vue";
import { CaretLeft, CaretRight } from "@element-plus/icons-vue";
import axios from "axios";
import { Plus, Delete, Edit } from "@element-plus/icons-vue";
import { formatTime } from "@/utils/format.js";
import Drawer from "@/components/Drawer.vue";
import { ElMessage } from "element-plus";
// 日期时间
const value2 = ref("");

const mealsList = ref([]);
const getMealsList = async () => {
  const res = await axios.get("/api/mealsList");
  mealsList.value = res.data;
  // console.log(mealsList.value);
};
//分页
let total = ref(0); //总条目数
let pageSize = ref(3); //一页显示几个
let currentPage = ref(1); //当前展示的页码
let showList = ref([]); //当前展示的列表
const getPaging = () => {
  total.value = mealsList.value.length;
  showList.value = mealsList.value.slice(
    (currentPage.value - 1) * pageSize.value,
    currentPage.value * pageSize.value
  );
  // console.log(showList.value);
};
//单击页码跳转对应的页面  val:单击的页码数
const handlePage = (val) => {
  currentPage.value = val;
  getPaging();
};
//新增/编辑
let drawerRef = ref(null);
let mealsData = ref({});
//表单数据校验
const formRules = ref({
  name: [{ required: true, message: "姓名不能为空", trigger: "submit" }],
  breakfast: [{ required: true, message: "早餐不能为空", trigger: "submit" }],
  lunch: [{ required: true, message: "午餐不能为空", trigger: "submit" }],
  dinner: [{ required: true, message: "晚餐不能为空", trigger: "submit" }],
  dessert: [{ required: true, message: "点心不能为空", trigger: "submit" }],
  fruit: [{ required: true, message: "水果不能为空", trigger: "submit" }],
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
    mealsData.value = {
      name: "",
      img: "https://pic1.zhimg.com/v2-b207fe0605358a922737e2d6781cbe56_r.jpg?source=1940ef5c",
      breakfast: "",
      lunch: "",
      dinner: "",
      dessert: "",
      fruit: "",
      date: Date(),
    };
  } else {
    //编辑==>通过id去获取商品信息
    mealsData.value = (await axios.get(`/api/mealsList/${id}`)).data;
  }
  //打开抽屉
  drawerRef.value.open();
};
// 提交
const formRef = ref(null);
const formSubmit = () => {
  //验证数据是否合法 :通过返回true ，不通过返回false
  formRef.value.validate(async (res) => {
    if (!res) return;
    //验证通过
    if (mode.value === "add") {
      await axios.post("/api/mealsList", mealsData.value);
      await getMealsList();
      getPaging();
      ElMessage.success("添加成功");
    } else {
      await axios.put(`/api/mealsList/${mark.value}`, mealsData.value);
      await getMealsList();
      getPaging();
      ElMessage.success("编辑成功");
    }
    //关闭抽屉
    drawerRef.value.close();
  });
};
// 删除
const delGoods = async (id) => {
  await axios.delete(`/api/mealsList/${id}`);
  await getMealsList();
  getPaging();
  ElMessage.success("删除成功");
};
// 取消
const formCancel = () => {
  mealsData.value = {};
  drawerRef.value.close();
};
// 搜索相关的响应式数据
const searchName = ref(""); // 搜索名字
const searchDate = ref([]); // 搜索日期范围

// 搜索方法
const handleSearch = async () => {
  try {
    // 验证搜索条件
    if (
      !searchName.value &&
      (!searchDate.value || searchDate.value.length !== 2)
    ) {
      ElMessage.warning("请输入姓名和选择日期范围");
      return;
    }

    const res = await axios.get("/api/mealsList");
    let filteredList = res.data;

    // 同时满足名字和日期条件
    filteredList = filteredList.filter((item) => {
      // 名字匹配条件
      const nameMatch = searchName.value
        ? item.name.includes(searchName.value)
        : true;

      // 日期匹配条件
      let dateMatch = true;
      if (searchDate.value && searchDate.value.length === 2) {
        const startDate = new Date(searchDate.value[0]).getTime();
        const endDate = new Date(searchDate.value[1]).getTime();
        const itemDate = new Date(item.date).getTime();
        dateMatch = itemDate >= startDate && itemDate <= endDate;
      }

      // 必须同时满足两个条件
      return nameMatch && dateMatch;
    });

    // 更新列表数据
    mealsList.value = filteredList;

    // 如果没有搜索结果
    if (filteredList.length === 0) {
      ElMessage.warning("未找到匹配的数据");
    } else {
      ElMessage.success(`找到 ${filteredList.length} 条匹配数据`);
    }

    // 重置分页到第一页
    currentPage.value = 1;
    getPaging();
  } catch (error) {
    console.error("搜索失败:", error);
    ElMessage.error("搜索失败");
  }
};

// 重置搜索
const resetSearch = () => {
  searchName.value = "";
  searchDate.value = [];
  getMealsList(); // 重新获取所有数据
};

// 添加序号计算方法
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1;
};

onMounted(async () => {
  await getMealsList();
  getPaging();
});
</script>

<template>
  <PageContainer>
    <template #main>
      <el-form class="form" :inline="true">
        <el-form-item label="姓名">
          <el-input v-model="searchName" placeholder="请输入姓名" clearable />
        </el-form-item>

        <el-form-item label="日期">
          <el-date-picker
            v-model="searchDate"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch"> 查询 </el-button>
          <el-button @click="resetSearch"> 重置 </el-button>
        </el-form-item>
      </el-form>
    </template>
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
      <el-table-column label="日期" prop="date">
        <template #default="{ row }">
          {{ formatTime(row.date) }}
        </template>
      </el-table-column>
      <el-table-column label="封面" v-slot="pic">
        <img :src="pic.row.img" width="90px" />
      </el-table-column>
      <el-table-column label="早餐" prop="breakfast" />
      <el-table-column label="午餐" prop="lunch" />
      <el-table-column label="晚餐" prop="dinner" />
      <el-table-column label="点心" prop="dessert" />
      <el-table-column label="水果" prop="fruit" />
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
            @click="delGoods(row.id)"
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
        <el-form :model="mealsData" :rules="formRules" ref="formRef">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="mealsData.name" />
          </el-form-item>
          <el-form-item label="早餐" prop="breakfast">
            <el-select v-model="mealsData.breakfast" placeholder="请选择早餐">
              <el-option label="面包" value="面包"></el-option>
              <el-option label="粥" value="粥"></el-option>
              <el-option label="油条" value="油条"></el-option>
              <el-option label="包子" value="包子"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="午餐" prop="lunch">
            <el-select v-model="mealsData.lunch" placeholder="请选择午餐">
              <el-option label="面条" value="面条"></el-option>
              <el-option label="煲仔饭" value="煲仔饭"></el-option>
              <el-option label="一荤一素" value="一荤一素"></el-option>
              <el-option label="两荤一素" value="两荤一素"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="晚餐" prop="dinner">
            <el-select v-model="mealsData.dinner" placeholder="请选择晚餐">
              <el-option label="一荤一素" value="油条"></el-option>
              <el-option label="两荤一素" value="两荤一素"></el-option>
              <el-option label="三荤一素" value="三荤一素"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="点心" prop="dessert">
            <el-select v-model="mealsData.dessert" placeholder="请选择点心">
              <el-option label="青团" value="青团"></el-option>
              <el-option label="绿豆沙" value="绿豆沙"></el-option>
              <el-option label="桂花糕" value="桂花糕"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="水果" prop="fruit">
            <el-select v-model="mealsData.fruit" placeholder="请选择水果">
              <el-option label="香蕉" value="香蕉"></el-option>
              <el-option label="苹果" value="苹果"></el-option>
              <el-option label="草莓" value="草莓"></el-option>
              <el-option label="冰糖桔" value="冰糖桔"></el-option>
              <el-option label="西瓜" value="西瓜"></el-option>
              <el-option label="百香果" value="百香果"></el-option>
              <el-option label="黄桃" value="黄桃"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="照片">
            <el-upload
              class="avatar-uploader"
              action=""
              :show-file-list="false"
            >
              <img class="avatar" v-if="mode === 'edit'" :src="mealsData.img" />
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
.form {
  display: flex;
  align-items: center;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;

  :deep(.el-form-item) {
    margin-bottom: 0;
    margin-right: 20px;

    &:last-child {
      margin-right: 0;
    }
  }

  :deep(.el-date-picker) {
    width: 360px;
  }

  :deep(.el-input) {
    width: 200px;
  }
}
.demo-datetime-picker-icon {
  display: flex;
  width: 100%;
  padding: 0;
  flex-wrap: wrap;
  justify-content: space-around;
  align-items: stretch;
}
.demo-datetime-picker-icon .block {
  padding: 30px 0;
  text-align: center;
}
.line {
  width: 1px;
  background-color: var(--el-border-color);
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.avatar {
  width: 200px;
  height: 200px;
  object-fit: cover;
}
</style>
