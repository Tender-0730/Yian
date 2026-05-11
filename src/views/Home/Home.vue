<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import * as echarts from "echarts";
import axios from "axios";

// 引用图表DOM
const careChart = ref(null);
const roomChart = ref(null);
const trendChart = ref(null);
const ageChart = ref(null); // 新增年龄分布图表

// 数据卡片
const dataCards = ref([
  {
    title: "总人数",
    number: 0,
    icon: "Bed",
    desc: "当前总人数",
    color: "#409EFF",
  },
  {
    title: "当前入住",
    number: 0,
    icon: "User",
    desc: "实时入住人数",
    color: "#67C23A",
  },
  {
    title: "特级护理",
    number: 0,
    icon: "Star",
    desc: "特级护理人数",
    color: "#E6A23C",
  },
  {
    title: "今日新增",
    number: 0,
    icon: "Plus",
    desc: "新增入住人数",
    color: "#F56C6C",
  },
]);

// 初始化护理级别分布图表
const initCareChart = (data) => {
  const chart = echarts.init(careChart.value);
  chart.setOption({
    tooltip: {
      trigger: "item",
      formatter: "{b}: {c}人 ({d}%)",
    },
    legend: {
      orient: "vertical",
      right: "5%",
      top: "middle",
      itemWidth: 10,
      itemHeight: 10,
      icon: "circle",
    },
    series: [
      {
        name: "护理级别",
        type: "pie",
        radius: ["40%", "70%"],
        center: ["40%", "50%"],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: "#fff",
          borderWidth: 2,
        },
        label: {
          show: true,
          position: "outside",
          formatter: "{b}\n{c}人",
          fontSize: 14,
        },
        labelLine: {
          length: 15,
          length2: 10,
          smooth: true,
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: "bold",
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: "rgba(0, 0, 0, 0.5)",
          },
        },
        data: [
          {
            value: data.special,
            name: "特级护理",
            itemStyle: { color: "#F56C6C" },
          },
          {
            value: data.medium,
            name: "中级护理",
            itemStyle: { color: "#E6A23C" },
          },
          {
            value: data.normal,
            name: "基础护理",
            itemStyle: { color: "#67C23A" },
          },
        ],
      },
    ],
  });
};

// 初始化房间使用情况图表
const initRoomChart = (data) => {
  const chart = echarts.init(roomChart.value);
  chart.setOption({
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "shadow",
      },
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: ["总房间", "已使用", "空闲"],
      axisLabel: {
        interval: 0,
      },
    },
    yAxis: {
      type: "value",
    },
    series: [
      {
        data: [
          {
            value: data.total,
            itemStyle: { color: "#409EFF" },
          },
          {
            value: data.used,
            itemStyle: { color: "#67C23A" },
          },
          {
            value: data.available,
            itemStyle: { color: "#909399" },
          },
        ],
        type: "bar",
        showBackground: true,
        backgroundStyle: {
          color: "rgba(180, 180, 180, 0.2)",
        },
        label: {
          show: true,
          position: "top",
          formatter: "{c}间",
        },
      },
    ],
  });
};

// 初始化入住趋势图表
const initTrendChart = (data) => {
  const chart = echarts.init(trendChart.value);
  chart.setOption({
    tooltip: {
      trigger: "axis",
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: data.months,
    },
    yAxis: {
      type: "value",
      axisLabel: {
        formatter: "{value}人",
      },
    },
    series: [
      {
        name: "入住人数",
        type: "line",
        smooth: true,
        data: data.numbers,
        areaStyle: {
          opacity: 0.3,
        },
        itemStyle: {
          color: "#409EFF",
        },
        lineStyle: {
          width: 3,
        },
      },
    ],
  });
};

// 初始化年龄分布图表
const initAgeChart = (data) => {
  const chart = echarts.init(ageChart.value);
  chart.setOption({
    tooltip: {
      trigger: "axis",
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: ["60以下", "60-70", "70-80", "80-90", "90以上"],
    },
    yAxis: {
      type: "value",
      axisLabel: {
        formatter: "{value}人",
      },
    },
    series: [
      {
        data: data,
        type: "bar",
        barWidth: "40%",
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "#83bff6" },
            { offset: 0.5, color: "#188df0" },
            { offset: 1, color: "#188df0" },
          ]),
        },
      },
    ],
  });
};

// 获取统计数据
const fetchDashboardData = async () => {
  try {
    const res = await axios.get("/api/MessageList");
    const messages = res.data;

    // 计算护理级别分布
    const careLevels = {
      special: messages.filter((m) => m.LevelOfcare === "特级护理").length,
      medium: messages.filter((m) => m.LevelOfcare === "中级护理").length,
      normal: messages.filter((m) => m.LevelOfcare === "基础护理").length,
    };

    // 更新数据卡片
    dataCards.value[0].number = messages.length;
    dataCards.value[1].number = messages.filter(
      (m) => m.status === "已入住"
    ).length;
    dataCards.value[2].number = careLevels.special;
    dataCards.value[3].number = messages.filter((m) => {
      const today = new Date().toISOString().split("T")[0];
      return m.createTime?.includes(today);
    }).length;

    // 初始化图表
    initCareChart(careLevels);

    const roomData = {
      total: 100,
      used: messages.length,
      available: 100 - messages.length,
    };
    initRoomChart(roomData);

    // 计算近6个月的趋势
    const now = new Date();
    const months = [];
    const numbers = [];
    for (let i = 5; i >= 0; i--) {
      const month = new Date(now.getFullYear(), now.getMonth() - i, 1);
      months.push(`${month.getMonth() + 1}月`);
      const monthData = messages.filter((m) => {
        const createTime = new Date(m.createTime);
        return (
          createTime.getMonth() === month.getMonth() &&
          createTime.getFullYear() === month.getFullYear()
        );
      });
      numbers.push(monthData.length);
    }

    initTrendChart({ months, numbers });

    // 计算年龄分布
    const ageGroups = [0, 0, 0, 0, 0]; // 60以下，60-70，70-80，80-90，90以上
    messages.forEach((m) => {
      const age = m.age ? parseInt(m.age) : 0;
      if (age < 60) ageGroups[0]++;
      else if (age < 70) ageGroups[1]++;
      else if (age < 80) ageGroups[2]++;
      else if (age < 90) ageGroups[3]++;
      else ageGroups[4]++;
    });

    initAgeChart(ageGroups);
  } catch (error) {
    console.error("获取数据失败:", error);
  }
};

// 监听窗口大小变化，重绘图表
const handleResize = () => {
  const charts = [
    echarts.getInstanceByDom(careChart.value),
    echarts.getInstanceByDom(roomChart.value),
    echarts.getInstanceByDom(trendChart.value),
    echarts.getInstanceByDom(ageChart.value),
  ];

  charts.forEach((chart) => chart?.resize());
};

onMounted(() => {
  fetchDashboardData();
  window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
});

// 在数据统计部分添加计算今日新增的逻辑
const getTodayCount = () => {
  if (!MessageList.value || MessageList.value.length === 0) {
    console.log("MessageList为空"); // 检查数据是否为空
    return 0;
  }

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);

  console.log("今天开始时间：", today);
  console.log("明天开始时间：", tomorrow);

  const todayRecords = MessageList.value.filter((item) => {
    try {
      const createTime = new Date(item.createTime);
      const isToday = createTime >= today && createTime < tomorrow;
      console.log(`记录时间：${item.createTime}, 是否今天：${isToday}`);
      return isToday;
    } catch (error) {
      console.error("日期处理错误:", error, item);
      return false;
    }
  });

  console.log("今日新增记录：", todayRecords);
  return todayRecords.length;
};

// 在统计数据中使用
const statistics = ref({
  totalBeds: 0, // 总床位
  currentOccupied: 0, // 当前入住
  specialCare: 0, // 特级护理
  todayNew: 0, // 今日新增
});

// 更新统计数据的方法
const updateStatistics = () => {
  console.log("开始更新统计数据");
  console.log("当前MessageList长度:", MessageList.value.length);

  statistics.value = {
    totalBeds: 100,
    currentOccupied: MessageList.value.length,
    specialCare: MessageList.value.filter(
      (item) => item.LevelOfcare === "特级护理"
    ).length,
    todayNew: getTodayCount(),
  };

  console.log("更新后的统计数据：", statistics.value);
};

// 在获取数据后更新统计
const getMessageList = async () => {
  try {
    const res = await axios.get("/api/MessageList");
    console.log("原始API数据：", res.data); // 查看原始数据

    // 确保每条数据都有createTime字段
    MessageList.value = (res.data || []).map((item) => {
      if (!item.createTime) {
        console.log("数据缺少createTime:", item); // 记录问题数据
      }
      return {
        ...item,
        createTime: item.createTime || new Date().toISOString(),
      };
    });

    console.log("处理后的MessageList:", MessageList.value); // 查看处理后的数据
    updateStatistics();
  } catch (error) {
    // console.error("获取数据失败:", error);
    // MessageList.value = []; // 确保发生错误时初始化为空数组
  }
};

// 初始化图表
// const initCharts = () => {
//   // ... 图表初始化代码 ...
// };

onMounted(async () => {
  await getMessageList();
});
</script>

<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in dataCards" :key="card.title">
        <el-card class="data-card" :body-style="{ padding: '20px' }">
          <div class="card-header">
            <span>{{ card.title }}</span>
            <el-icon
              :class="card.icon"
              :style="{ color: card.color }"
              class="card-icon"
            ></el-icon>
          </div>
          <div class="card-number" :style="{ color: card.color }">
            {{ card.number }}
          </div>
          <div class="card-footer">{{ card.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>护理级别分布</span>
            </div>
          </template>
          <div ref="careChart" class="chart"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>房间使用情况</span>
            </div>
          </template>
          <div ref="roomChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>入住趋势统计</span>
            </div>
          </template>
          <div ref="trendChart" class="chart"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>年龄分布统计</span>
            </div>
          </template>
          <div ref="ageChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.chart-row {
  margin-top: 20px;
}

.chart {
  height: 300px;
  width: 100%;
}

.data-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.data-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #909399;
}

.card-icon {
  font-size: 24px;
}

.card-number {
  font-size: 28px;
  font-weight: bold;
  margin: 10px 0;
}

.card-footer {
  color: #909399;
  font-size: 12px;
}

:deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
  font-weight: bold;
}
</style>
