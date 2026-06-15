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
        <el-form-item label="作者">
          <el-input v-model="search3" placeholder="请输入作者" clearable>
            <template #prefix><el-icon class="el-input__icon"><search /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="margin-left: 1%" @click="load" size="mini">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button size="mini" type="danger" @click="clear">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right" v-if="numOfOutDataBook!=0">
          <el-popconfirm
              confirm-button-text="查看"
              cancel-button-text="取消"
              title="您有图书已逾期，请尽快归还"
              @confirm="toLook"
          >
            <template #reference>
              <el-button type="warning">逾期通知</el-button>
            </template>
          </el-popconfirm>
        </el-form-item>
      </el-form>
    </div>

    <!-- 按钮-->
    <div style="margin: 10px 0;">
      <el-button type="primary" @click="add" v-if="user.role == 1">上架</el-button>
      <el-popconfirm title="确认下架?" @confirm="deleteBatch" v-if="user.role == 1">
        <template #reference>
          <el-button type="danger" size="mini">批量下架</el-button>
        </template>
      </el-popconfirm>
    </div>

    <!-- 数据字段-->
    <el-table :data="tableData" stripe border @selection-change="handleSelectionChange">
      <el-table-column v-if="user.role ==1" type="selection" width="55"></el-table-column>
      <el-table-column prop="isbn" label="图书编号" sortable />
      <el-table-column prop="name" label="图书名称" />
      <el-table-column prop="price" label="价格" sortable/>
      <el-table-column prop="author" label="作者" />
      <el-table-column prop="publisher" label="出版社" />
      <el-table-column prop="createTime" label="出版时间" sortable/>
      <el-table-column prop="borrownum" label="总借阅次数" sortable/>
      <el-table-column prop="status" label="状态">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status == 0" type="warning">已借阅</el-tag>
          <el-tag v-else type="success">未借阅</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="180">
        <template v-slot="scope">
          <el-button size="mini" @click="handleEdit(scope.row)" v-if="user.role == 1">修改</el-button>
          <el-popconfirm title="确认下架?" @confirm="handleDelete(scope.row.id)" v-if="user.role == 1">
            <template #reference>
              <el-button type="danger" size="mini">下架</el-button>
            </template>
          </el-popconfirm>
          <el-button size="mini" @click="handleLend(scope.row)" v-if="user.role == 2 && scope.row.status == 1">借阅</el-button>
          <el-popconfirm title="确认还书?" @confirm="handleReturn(scope.row)" v-if="user.role == 2 && isBorrowed(scope.row.isbn)">
            <template #reference>
              <el-button type="primary" size="mini">还书</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 逾期详情对话框 -->
    <el-dialog v-model="dialogVisible3" title="逾期详情" width="50%">
      <el-table :data="outDateBook" style="width: 100%">
        <el-table-column prop="isbn" label="图书编号" />
        <el-table-column prop="bookName" label="书名" />
        <el-table-column prop="lendtime" label="借阅日期" />
        <el-table-column prop="deadtime" label="截至日期" />
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="dialogVisible3 = false">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分页 -->
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

    <!-- 上架/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="30%">
      <el-form :model="form" label-width="120px">
        <el-form-item label="图书编号">
          <el-input v-model="form.isbn"></el-input>
        </el-form-item>
        <el-form-item label="图书名称">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="form.price"></el-input>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="form.author"></el-input>
        </el-form-item>
        <el-form-item label="出版社">
          <el-input v-model="form.publisher"></el-input>
        </el-form-item>
        <el-form-item label="出版时间">
          <el-date-picker value-format="YYYY-MM-DD" type="date" style="width: 100%" clearable v-model="form.createTime"></el-date-picker>
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
  name: 'Book',
  created() {
    let userJson = sessionStorage.getItem("user")
    if (!userJson) {
      router.push("/login")
      return
    }
    let userStr = sessionStorage.getItem("user") || "{}"
    this.user = JSON.parse(userStr)
    this.load()
    this.loadUserBorrowStatus()
  },
  data() {
    return {
      form: {},
      dialogVisible: false,
      dialogTitle: '上架书籍',
      search1: '',
      search2: '',
      search3: '',
      total: 0,
      currentPage: 1,
      pageSize: 10,
      tableData: [],
      user: {},
      ids: [],
      currentBorrowList: [],
      isbnArray: [],
      number: 0,
      outDateBook: [],
      numOfOutDataBook: 0,
      dialogVisible3: false,
      flag: true,
      maxBorrowCount: 10  // 默认值
    }
  },
  methods: {
    // 加载用户当前借阅状态 - 直接调用后端完整URL
    async loadUserBorrowStatus() {
      if (this.user.role == 2 && this.user.id) {
        try {
          // 1. 先获取最大借阅数量配置
          try {
            const configRes = await request.get("http://localhost:8083/borrow/config/maxBorrow")
            console.log("配置返回:", configRes)
            if (configRes.code == 0 && configRes.data && configRes.data.maxBorrow) {
              this.maxBorrowCount = configRes.data.maxBorrow
              console.log("最大借阅数量配置:", this.maxBorrowCount)
            }
          } catch (configError) {
            console.error("获取配置失败，使用默认值", configError)
            // 保持默认值 10
          }
          
          // 2. 获取借阅列表
          const res = await request.get("http://localhost:8083/bookwithuser/user/" + this.user.id)
          if (res.code == 0) {
            this.currentBorrowList = res.data || []
            this.isbnArray = this.currentBorrowList.map(item => item.isbn)
            this.number = this.currentBorrowList.length
            
            // 检查逾期
            const nowDate = new Date()
            this.outDateBook = []
            this.numOfOutDataBook = 0
            for (let i = 0; i < this.currentBorrowList.length; i++) {
              const book = this.currentBorrowList[i]
              const deadtime = new Date(book.deadtime)
              if (deadtime < nowDate) {
                this.outDateBook.push({
                  isbn: book.isbn,
                  bookName: book.bookName,
                  deadtime: book.deadtime,
                  lendtime: book.lendtime
                })
                this.numOfOutDataBook++
              }
            }
          }
        } catch (error) {
          console.error("加载借阅状态失败", error)
        }
      }
    },

    // 判断某本书是否已被当前用户借阅
    isBorrowed(isbn) {
      return this.isbnArray && this.isbnArray.indexOf(isbn) !== -1
    },

    handleSelectionChange(val) {
      this.ids = val.map(v => v.id)
    },

    async deleteBatch() {
      if (!this.ids.length) {
        ElMessage.warning("请选择数据！")
        return
      }
      try {
        const res = await request.post("/book/deleteBatch", this.ids)
        if (res.code === '0') {
          ElMessage.success("批量下架成功")
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
        const res = await request.get("/book", {
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
        console.error("加载图书失败", error)
      }

      if (this.user.role == 2) {
        try {
          const res = await request.get("/user/checkAlow/" + this.user.id)
          this.flag = (res.code == 0)
        } catch (error) {
          this.flag = false
        }
      }
    },

    clear() {
      this.search1 = ""
      this.search2 = ""
      this.search3 = ""
      this.load()
    },

    async handleDelete(id) {
      try {
        const res = await request.delete("/book/" + id)
        if (res.code == 0) {
          ElMessage.success("下架成功")
        } else {
          ElMessage.error(res.msg)
        }
        this.load()
      } catch (error) {
        ElMessage.error("操作失败")
      }
    },

    // 借阅图书 - 直接调用后端完整URL
    async handleLend(row) {
      if (this.user.phone == null || this.user.phone === "") {
        ElMessage.error("借阅失败! 请先将个人信息补充完整")
        this.$router.push("/person")
        return
      }
      
      // 使用从后端获取的配置值
      if (this.number >= this.maxBorrowCount) {
        ElMessage.warning("您不能再借阅更多的书籍了（最多" + this.maxBorrowCount + "本）")
        return
      }
      if (this.numOfOutDataBook > 0) {
        ElMessage.warning("请先归还逾期书籍")
        return
      }
      if (!this.flag) {
        ElMessage.error("您没有借阅权限，请等待管理员审核")
        return
      }

      try {
        const res = await request.post("http://localhost:8083/borrow/borrow", null, {
          params: {
            userId: this.user.id,
            isbn: row.isbn
          }
        })
        if (res.code == 0) {
          ElMessage.success("借阅成功")
          await this.loadUserBorrowStatus()
          await this.load()
        } else {
          ElMessage.error(res.msg)
        }
      } catch (error) {
        ElMessage.error("借阅失败，请重试")
      }
    },

    // 还书 - 直接调用后端完整URL
    async handleReturn(row) {
      if (!this.isBorrowed(row.isbn)) {
        ElMessage.warning("您没有借阅这本书")
        return
      }
      try {
        const res = await request.put("http://localhost:8083/borrow/return", null, {
          params: {
            userId: this.user.id,
            isbn: row.isbn
          }
        })
        if (res.code == 0) {
          ElMessage.success("还书成功")
          await this.loadUserBorrowStatus()
          await this.load()
        } else {
          ElMessage.error(res.msg)
        }
      } catch (error) {
        ElMessage.error("还书失败，请重试")
      }
    },

    add() {
      this.dialogVisible = true
      this.dialogTitle = '上架书籍'
      this.form = {}
    },

    async save() {
      if (!this.form.isbn || !this.form.name) {
        ElMessage.warning("请填写完整的图书信息")
        return
      }
      try {
        let res
        if (this.form.id) {
          res = await request.put("/book", this.form)
          if (res.code == 0) {
            ElMessage.success('修改成功')
          } else {
            ElMessage.error(res.msg)
          }
        } else {
          this.form.borrownum = 0
          this.form.status = 1
          res = await request.post("/book", this.form)
          if (res.code == 0) {
            ElMessage.success('上架成功')
          } else {
            ElMessage.error(res.msg)
          }
        }
        if (res.code == 0) {
          this.dialogVisible = false
          this.load()
        }
      } catch (error) {
        ElMessage.error("操作失败")
      }
    },

    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogVisible = true
      this.dialogTitle = '修改书籍信息'
    },

    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },

    handleCurrentChange(pageNum) {
      this.currentPage = pageNum
      this.load()
    },

    toLook() {
      this.dialogVisible3 = true
    }
  }
}
</script>