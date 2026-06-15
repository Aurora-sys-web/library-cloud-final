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
        <el-form-item label="读者编号">
          <el-input v-model="search3" placeholder="请输入读者编号" clearable>
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
    <div style="margin: 10px 0;" v-if="user.role == 1">
      <el-popconfirm title="确认删除?" @confirm="deleteBatch">
        <template #reference>
          <el-button type="danger" size="mini">批量删除</el-button>
        </template>
      </el-popconfirm>
    </div>

    <!-- 数据字段-->
    <el-table :data="tableData" stripe border @selection-change="handleSelectionChange">
      <el-table-column v-if="user.role ==1" type="selection" width="55"></el-table-column>
      <el-table-column prop="isbn" label="图书编号" sortable />
      <el-table-column prop="bookname" label="图书名称" />
      <el-table-column prop="readerId" label="读者编号" sortable/>
      <el-table-column prop="lendTime" label="借阅时间" sortable/>
      <el-table-column prop="returnTime" label="归还时间" sortable/>
      <el-table-column prop="status" label="状态">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status == 0" type="warning">未归还</el-tag>
          <el-tag v-else type="success">已归还</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="user.role === 1" label="操作" width="230px">
        <template v-slot="scope">
          <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
          <el-popconfirm title="确认删除?" @confirm="handleDelete(scope.row)">
            <template #reference>
              <el-button type="danger" size="mini">删除记录</el-button>
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

    <!-- 修改借阅记录对话框 -->
    <el-dialog v-model="dialogVisible" title="修改借阅记录" width="30%">
      <el-form :model="form" label-width="120px">
        <el-form-item label="借阅时间">
          <el-date-picker
              v-model="form.lendTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="归还时间">
          <el-date-picker
              v-model="form.returnTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="是否归还" prop="status">
          <el-radio v-model="form.status" label="0">未归还</el-radio>
          <el-radio v-model="form.status" label="1">已归还</el-radio>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from "../utils/request";
import { ElMessage } from "element-plus";
import router from "@/router";

export default {
  created() {
    let userJson = sessionStorage.getItem("user")
    if (!userJson) {
      router.push("/login")
    }
    let userStr = sessionStorage.getItem("user") || "{}"
    this.user = JSON.parse(userStr)
    this.load()
  },
  name: 'LendRecord',
  data() {
    return {
      form: {},
      search1: '',
      search2: '',
      search3: '',
      total: 0,
      currentPage: 1,
      pageSize: 10,
      tableData: [],
      user: {},
      forms: [],
      dialogVisible: false
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
        const res = await request.post("http://localhost:8083/LendRecord/deleteRecords", this.forms)
        if (res.code === '0') {
          ElMessage.success("批量删除成功")
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
        const res = await request.get("http://localhost:8083/LendRecord", {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            search1: this.search1,
            search2: this.search2,
            search3: this.search3
          }
        })
        if (res.code == 0) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      } catch (error) {
        console.error("加载失败", error)
      }
    },
    async save() {
      try {
        let res
        // 判断是否有 id（编辑操作）
        if (this.form.id) {
          // 更新操作
          res = await request.put("http://localhost:8083/LendRecord/" + this.form.isbn, this.form)
          if (res.code == 0) {
            ElMessage.success('更新成功')
          } else {
            ElMessage.error(res.msg)
            return
          }
        } else {
          // 新增操作
          res = await request.post("http://localhost:8083/LendRecord", this.form)
          if (res.code == 0) {
            ElMessage.success('新增成功')
          } else {
            ElMessage.error(res.msg)
            return
          }
        }
        this.dialogVisible = false
        this.load()
      } catch (error) {
        console.error("保存失败:", error)
        ElMessage.error("操作失败")
      }
    },
    clear() {
      this.search1 = ""
      this.search2 = ""
      this.search3 = ""
      this.load()
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogVisible = true
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },
    handleCurrentChange(pageNum) {
      this.currentPage = pageNum
      this.load()
    },
    async handleDelete(row) {
      try {
        const res = await request.post("http://localhost:8083/LendRecord/deleteRecord", row)
        if (res.code == 0) {
          ElMessage.success("删除成功")
          this.load()
        } else {
          ElMessage.error(res.msg)
        }
      } catch (error) {
        ElMessage.error("操作失败")
      }
    }
  }
}
</script>