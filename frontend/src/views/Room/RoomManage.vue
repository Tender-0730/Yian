<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listBuildings, listRoomsByBuilding, listBedsByRoom, checkIn, checkOut, createRoom } from '@/api/room'
import { pageResidents } from '@/api/resident'

const buildings = ref([])
const rooms = ref([])
const beds = ref([])
const residents = ref([])
const activeBuildingId = ref(null)
const activeRoomId = ref(null)

const activeBuilding = computed(() => buildings.value.find(b => b.id === activeBuildingId.value))

// 按楼层分组房间
const roomsByFloor = computed(() => {
  const map = {}
  for (const r of rooms.value) {
    const f = r.floor || 1
    if (!map[f]) map[f] = []
    map[f].push(r)
  }
  return Object.entries(map).sort(([a], [b]) => +a - +b)
})

// 新增房间
const roomDialog = ref(false)
const roomForm = reactive({ floor: 1, roomNumber: '', roomType: 'DOUBLE', capacity: 2, description: '' })
const roomFormRef = ref(null)
const roomRules = {
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入床位数', trigger: 'blur' }]
}

// 办理入住
const checkInDialog = ref(false)
const checkInForm = reactive({ residentId: null, bedId: null, checkInDate: '', remark: '' })
const checkInFormRef = ref(null)
const checkInRules = {
  residentId: [{ required: true, message: '请选择老人', trigger: 'change' }],
  bedId: [{ required: true, message: '请选择床位', trigger: 'change' }]
}

const statusTag = (s) => s === 'AVAILABLE' ? 'success' : 'warning'
const statusLabel = (s) => s === 'AVAILABLE' ? '空闲' : '已占用'

const loadBuildings = async () => { buildings.value = await listBuildings() }

const selectBuilding = async (id) => {
  activeBuildingId.value = id
  activeRoomId.value = null
  beds.value = []
  rooms.value = await listRoomsByBuilding(id)
}

const selectRoom = async (id) => {
  activeRoomId.value = id
  beds.value = await listBedsByRoom(id)
}

const handleCreateRoom = async () => {
  const valid = await roomFormRef.value.validate().catch(() => false)
  if (!valid) return
  await createRoom({ ...roomForm, buildingId: activeBuildingId.value })
  ElMessage.success('新增房间成功')
  roomDialog.value = false
  Object.assign(roomForm, { floor: 1, roomNumber: '', roomType: 'DOUBLE', capacity: 2, description: '' })
  selectBuilding(activeBuildingId.value)
}

const openCheckIn = (bed) => {
  checkInForm.bedId = bed.id
  checkInForm.residentId = null
  checkInForm.checkInDate = ''
  checkInForm.remark = ''
  checkInDialog.value = true
}

const handleCheckIn = async () => {
  const valid = await checkInFormRef.value.validate().catch(() => false)
  if (!valid) return
  await checkIn(checkInForm)
  ElMessage.success('入住办理成功')
  checkInDialog.value = false
  selectRoom(activeRoomId.value)
  loadBuildings()
}

const handleCheckOut = async (bed) => {
  await ElMessageBox.confirm(`确定为 ${bed.bedNumber} 办理退住吗？`, '确认退住', { type: 'warning' })
  await checkOut(bed.recordId)
  ElMessage.success('退住办理成功')
  selectRoom(activeRoomId.value)
  loadBuildings()
}

onMounted(async () => {
  await loadBuildings()
  const res = await pageResidents({ size: 999 })
  residents.value = res.records || []
})
</script>

<template>
  <div class="room-page">
    <el-row :gutter="16">
      <!-- 楼栋列表 -->
      <el-col :span="5">
        <div class="panel-card">
          <div class="panel-header">楼栋</div>
          <div class="panel-body">
            <div v-if="!buildings.length" class="empty-hint">暂无楼栋</div>
            <div
              v-for="b in buildings" :key="b.id"
              class="list-item"
              :class="{ active: activeBuildingId === b.id }"
              @click="selectBuilding(b.id)"
            >
              <div class="item-title">{{ b.buildingName }}</div>
              <div class="item-sub">{{ b.floorCount }} 层 · {{ b.description || '' }}</div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 房间列表（按楼层分组） -->
      <el-col :span="8">
        <div class="panel-card">
          <div class="panel-header">
            房间
            <el-button v-if="activeBuildingId" size="small" :icon="Plus" type="primary" link @click="roomDialog = true">新增</el-button>
          </div>
          <div class="panel-body">
            <div v-if="!activeBuildingId" class="empty-hint">请先选择楼栋</div>
            <div v-else-if="!rooms.length" class="empty-hint">暂无房间</div>
            <template v-else>
              <div v-for="[floor, floorRooms] in roomsByFloor" :key="floor" class="floor-group">
                <div class="floor-label">{{ floor }}F</div>
                <div
                  v-for="r in floorRooms" :key="r.id"
                  class="list-item"
                  :class="{ active: activeRoomId === r.id }"
                  @click="selectRoom(r.id)"
                >
                  <div class="item-row">
                    <span class="item-title">{{ r.roomNumber }}</span>
                    <el-tag v-if="r.roomType === 'ACTIVITY'" size="small" type="info">
                      {{ r.roomType === 'ACTIVITY' ? '活动' : r.roomType === 'SINGLE' ? '单人' : '双人' }}
                    </el-tag>
                    <el-tag v-else size="small">
                      {{ r.roomType === 'ACTIVITY' ? '活动' : r.roomType === 'SINGLE' ? '单人' : '双人' }}
                    </el-tag>
                  </div>
                  <div class="item-sub" v-if="r.roomType !== 'ACTIVITY'">
                    已住 {{ r.occupied }}/{{ r.capacity }} · {{ r.description || '' }}
                  </div>
                  <div class="item-sub" v-else>活动室</div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </el-col>

      <!-- 床位列表 -->
      <el-col :span="11">
        <div class="panel-card">
          <div class="panel-header">床位管理</div>
          <div class="panel-body">
            <div v-if="!activeRoomId" class="empty-hint">请先选择房间</div>
            <el-table v-else :data="beds" border stripe size="small">
              <el-table-column prop="bedNumber" label="床位号" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="入住老人" min-width="120">
                <template #default="{ row }">
                  <template v-if="row.residentName">
                    {{ row.residentName }}
                  </template>
                  <span v-else style="color: #909399">-</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160" align="center">
                <template #default="{ row }">
                  <el-button v-if="row.status === 'AVAILABLE'" size="small" type="primary" @click="openCheckIn(row)">办理入住</el-button>
                  <el-button v-else size="small" type="danger" @click="handleCheckOut(row)">办理退住</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 新增房间弹窗 -->
    <el-dialog v-model="roomDialog" title="新增房间" width="420px" destroy-on-close>
      <el-form ref="roomFormRef" :model="roomForm" :rules="roomRules" label-width="80px">
        <el-form-item label="楼层" prop="floor">
          <el-input-number v-model="roomForm.floor" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="房间号" prop="roomNumber">
          <el-input v-model="roomForm.roomNumber" placeholder="如：201" />
        </el-form-item>
        <el-form-item label="房型" prop="roomType">
          <el-select v-model="roomForm.roomType" style="width: 100%">
            <el-option label="单人间" value="SINGLE" />
            <el-option label="双人间" value="DOUBLE" />
            <el-option label="活动室" value="ACTIVITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="床位数" prop="capacity">
          <el-input-number v-model="roomForm.capacity" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roomForm.description" placeholder="如：朝南、带阳台" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roomDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateRoom">保存</el-button>
      </template>
    </el-dialog>

    <!-- 办理入住弹窗 -->
    <el-dialog v-model="checkInDialog" title="办理入住" width="420px" destroy-on-close>
      <el-form ref="checkInFormRef" :model="checkInForm" :rules="checkInRules" label-width="80px">
        <el-form-item label="床位号">
          <el-input :model-value="checkInForm.bedId ? beds.find(b => b.id === checkInForm.bedId)?.bedNumber : ''" disabled />
        </el-form-item>
        <el-form-item label="老人" prop="residentId">
          <el-select v-model="checkInForm.residentId" placeholder="请选择老人" filterable style="width: 100%">
            <el-option v-for="r in residents" :key="r.id" :label="`${r.name}（${r.gender === 1 ? '男' : '女'}·${r.age}岁）`" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期" prop="checkInDate">
          <el-date-picker v-model="checkInForm.checkInDate" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="checkInForm.remark" placeholder="可选填写" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkInDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCheckIn">确认入住</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.room-page {
  animation: fadeIn 0.35s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.panel-card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  min-height: 400px;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 16px;
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    border-bottom: 1px solid #ebeef5;
  }

  .panel-body {
    padding: 8px;
    max-height: calc(100vh - 280px);
    overflow-y: auto;
  }
}

.empty-hint {
  color: #909399;
  text-align: center;
  padding: 40px 20px;
  font-size: 14px;
}

.list-item {
  padding: 12px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;

  &:hover { background: #f5f7fa; }

  &.active {
    background: linear-gradient(135deg, #ecf5ff, #e8f9e8);
    .item-title { color: #409eff; font-weight: 600; }
  }

  .item-title { font-size: 14px; color: #303133; }
  .item-sub { font-size: 12px; color: #909399; margin-top: 2px; }
  .item-row { display: flex; justify-content: space-between; align-items: center; }
}

.floor-group {
  margin-bottom: 8px;

  .floor-label {
    font-size: 12px;
    color: #909399;
    padding: 4px 10px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
}
</style>
