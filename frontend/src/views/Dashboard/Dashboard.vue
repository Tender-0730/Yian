<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { User, HomeFilled, CirclePlus, TrendCharts } from '@element-plus/icons-vue'
import { getStats } from '@/api/dashboard'

const stats = ref({})
const loading = ref(false)
const careChartRef = ref(null)
const ageChartRef = ref(null)
let careChart = null
let ageChart = null

const statCards = computed(() => [
  {
    label: '在册老人',
    value: stats.value.totalResidents || 0,
    unit: '人',
    icon: User,
    color: '#409eff',
    bg: 'linear-gradient(135deg, #409eff15, #409eff08)',
  },
  {
    label: '在院老人',
    value: stats.value.checkedInCount || 0,
    unit: '人',
    icon: HomeFilled,
    color: '#67c23a',
    bg: 'linear-gradient(135deg, #67c23a15, #67c23a08)',
  },
  {
    label: '特级护理',
    value: stats.value.specialCareCount || 0,
    unit: '人',
    icon: CirclePlus,
    color: '#e6a23c',
    bg: 'linear-gradient(135deg, #e6a23c15, #e6a23c08)',
  },
  {
    label: '入住率',
    value: stats.value.occupancyRate || '0%',
    unit: '',
    icon: TrendCharts,
    color: '#9b59b6',
    bg: 'linear-gradient(135deg, #9b59b615, #9b59b608)',
  },
])

const initCareChart = () => {
  if (!careChartRef.value || !stats.value.careLevelDistribution) return
  if (careChart) careChart.dispose()
  careChart = echarts.init(careChartRef.value)
  careChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 人 ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#606266' } },
    series: [
      {
        name: '护理级别',
        type: 'pie',
        radius: ['45%', '72%'],
        center: ['50%', '45%'],
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
        data: stats.value.careLevelDistribution,
      },
    ],
  })
}

const initAgeChart = () => {
  if (!ageChartRef.value || !stats.value.ageDistribution) return
  if (ageChart) ageChart.dispose()
  ageChart = echarts.init(ageChartRef.value)
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#9b59b6']
  ageChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: 10, left: 0, right: 0, bottom: 0, containLabel: true },
    xAxis: { type: 'category', data: stats.value.ageDistribution.map(i => i.name), axisLabel: { color: '#606266' } },
    yAxis: { type: 'value', show: false },
    series: [
      {
        type: 'bar',
        data: stats.value.ageDistribution.map((i, idx) => ({
          value: i.value,
          itemStyle: { color: colors[idx % colors.length], borderRadius: [6, 6, 0, 0] },
        })),
        barMaxWidth: 80,
        barCategoryGap: '30%',
      },
    ],
  })
}

const renderCharts = async () => {
  await nextTick()
  initCareChart()
  initAgeChart()
}

let resizeTimer = null
const handleResize = () => {
  clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    careChart?.resize()
    ageChart?.resize()
  }, 200)
}

onMounted(async () => {
  loading.value = true
  try {
    stats.value = await getStats()
    await renderCharts()
  } finally {
    loading.value = false
  }
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  careChart?.dispose()
  ageChart?.dispose()
})
</script>

<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div v-for="card in statCards" :key="card.label" class="stat-card" :style="{ background: card.bg }">
        <div class="stat-icon" :style="{ background: card.color }">
          <el-icon :size="24"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-value" :style="{ color: card.color }">
            {{ card.value }}<span class="stat-unit">{{ card.unit }}</span>
          </div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-grid">
      <div class="chart-card">
        <div class="chart-header">护理级别分布</div>
        <div ref="careChartRef" class="chart-box" />
      </div>
      <div class="chart-card">
        <div class="chart-header">年龄分布</div>
        <div ref="ageChartRef" class="chart-box" />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.dashboard {
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }

  .stat-icon {
    width: 52px;
    height: 52px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
  }

  .stat-body {
    .stat-value {
      font-size: 28px;
      font-weight: 700;
      line-height: 1.2;
    }
    .stat-unit {
      font-size: 14px;
      font-weight: 400;
      margin-left: 2px;
      color: #909399;
    }
    .stat-label {
      font-size: 13px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 16px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #ebeef5;

  .chart-header {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
    padding-left: 4px;
    border-left: 3px solid #409eff;
  }

  .chart-box {
    height: 300px;
  }
}
</style>
