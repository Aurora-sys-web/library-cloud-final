<template>
  <div class="home" style="padding: 10px">
    <!-- 搜索-->
    <div style="margin: 10px 0;">
      <el-form inline="true" size="small">
        <el-form-item label="图书编号">
          <el-input v-model="search1" placeholder="请输入图书编号" clearable>
            <template #prefix><el-icon class="el-input__icon"><search/></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="图书名称">
          <el-input v-model="search2" placeholder="请输入图书名称" clearable>
            <template #prefix><el-icon class="el-input__icon"><search /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="借阅者" v-if="user.role == 1">
          <el-input v-model="search3" placeholder="请输入借阅者昵称" clearable>
            <template #prefix><el-icon class="el-input__icon"><search /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load" size="mini">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button size="mini" type="danger" @click="clear">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 按钮-->
    <div style="margin: 10px 0;">
      <el-popconfirm title="确认归还?" @confirm="deleteBatch" v-if="user.role == 1">
        <template #reference>
          <el-button type="danger" size="mini">批量归还</el-button>
        </template>
      </el-popconfirm>
    </div>

    <!-- 数据字段-->
    <el-table :data="tableData" stripe border @selection-change="handleSelectionChange">
      <el-table-column v-if="user.role ==1" type="selection" width="55"></el-table-column>
      <el-table-column prop="isbn" label="图书编号" sortable />
      <el-table-column prop="bookName" label="图书名称" />
      <el-table-column prop="nickName" label="借阅者" />
      <el-table-column prop="lendtime" label="借阅时间" />
      <el-table-column prop="deadtime" label="最迟归还日期" />
      <el-table-column prop="prolong" label="可续借次数" />
      <el-table-column fixed="right" label="操作">
        <template v-slot="scope">
          <el-button size="mini" @click="handleEdit(scope.row)" v-if="user.role == 1">修改</el-button>
          <el-popconfirm title="确认归还?" @confirm="handleDelete(scope.row)" v-if="user.role == 1">
            <template #reference>
              <el-button type="danger" size="mini">归还</el-button>
            </template>
          </el-popconfirm>
          <el-popconfirm title="确认续借(续借一次延长30天)?" @confirm="handlereProlong(scope.row)" v-if="user.role == 2" :disabled="scope.row.prolong == 0">
            <template #reference>
              <el-button type="success" size="mini" :disabled="scope.row.prolong == 0">续借</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页-->
    <div style="margin: 10px 0">
      <el-pagination
          v-model:currentPage="currentPage"
          :page-sizes="[5, 10, 20]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 修改借阅信息对话框 -->
    <el-dialog v-model="dialogVisible2" title="修改借阅信息" width="30%">
      <el-form :model="form" label-width="120px">
        <el-form-item label="图书编号">
          <el-input v-model="form.isbn"></el-input>
        </el-form-item>
        <el-form-item label="图书名称">
          <el-input v-model="form.bookName"></el-input>
        </el-form-item>
        <el-form-item label="借阅者">
          <el-input v-model="form.nickName"></el-input>
        </el-form-item>
        <el-form-item label="续借次数">
          <el-input v-model="form.prolong"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible2 = false">取消</el-button>
          <el-button type="primary" @click="save">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from "../utils/request";
import { ElMessage } from "element-plus";
import moment from "moment";
import router from "@/router";

export default {
  created() {
    let userStr = sessionStorage.getItem("user") || "{}"
    this.user = JSON.parse(userStr)
    let userJson = sessionStorage.getItem("user")
    if (!userJson) {
      router.push("/login")
    }
    this.load()
  },
  name: 'bookwithuser',
  data() {
    return {
      form: {},
      dialogVisible2: false,
      search1: '',
      search2: '',
      search3: '',
      total: 0,
      currentPage: 1,
      pageSize: 10,
      tableData: [],
      user: {},
      forms: [],
    }
  },
  methods: {
    handleSelectionChange(val) {
      this.forms = val
    },
    async deleteBatch() {
      if (!this.forms.length) {
        ElMessage.warning("请选择数据！")
        return
      }
      try {
        const res = await request.post("http://localhost:8083/bookwithuser/deleteRecords", this.forms)
        if (res.code === '0') {
          ElMessage.success("批量归还成功")
          this.load()
        } else {
          ElMessage.error(res.msg)
        }
      } catch (error) {
        ElMessage.error("操作失败")
      }
    },
    async load() {
      try {
        let params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          search1: this.search1,
          search2: this.search2,
        }
        
        if (this.user.role == 1) {
          params.search3 = this.search3
        } else {
          params.search3 = this.user.id
        }
        
        const res = await request.get("http://localhost:8083/bookwithuser", { params })
        if (res.code == 0) {
          this.tableData = res.data.records || []
          this.total = res.data.total || 0
        }
      } catch (error) {
        console.error("加载失败", error)
      }
    },
    clear() {
      this.search1 = ""
      this.search2 = ""
      this.search3 = ""
      this.load()
    },
    async handleDelete(row) {
      try {
        const res = await request.post("http://localhost:8083/bookwithuser/deleteRecord", row)
        if (res.code == 0) {
          ElMessage.success("归还成功")
          this.load()
        } else {
          ElMessage.error(res.msg)
        }
      } catch (error) {
        ElMessage.error("操作失败")
      }
    },
    async handlereProlong(row) {
      try {
        var nowDate = new Date(row.deadtime);
        nowDate.setDate(nowDate.getDate() + 30);
        row.deadtime = moment(nowDate).format("YYYY-MM-DD HH:mm:ss");
        row.prolong = row.prolong - 1;
        
        const res = await request.post("http://localhost:8083/bookwithuser", row)
        if (res.code == 0) {
          ElMessage.success('续借成功')
          this.load()
        } else {
          ElMessage.error(res.msg)
        }
      } catch (error) {
        ElMessage.error("续借失败")
      }
    },
    async save() {
      try {
        const res = await request.post("http://localhost:8083/bookwithuser", this.form)
        if (res.code == 0) {
          ElMessage.success('修改信息成功')
          this.load()
          this.dialogVisible2 = false
        } else {
          ElMessage.error(res.msg)
        }
      } catch (error) {
        ElMessage.error("操作失败")
      }
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogVisible2 = true
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },
    handleCurrentChange(pageNum) {
      this.currentPage = pageNum
      this.load()
    },
  },
}
</script>