<template>
  <div class="main-content">
    <div>通信费计算</div>
    <el-divider></el-divider>
    <div style="margin-bottom: 12px; height: 32px">
      <el-upload
        action
        accept=".xlsx,.xls"
        :show-file-list="false"
        :auto-upload="false"
        :on-change="handle"
        style="display: inline; margin-right: 20px"
      >
        <template #trigger>
          <el-button type="primary">导入文件</el-button>
        </template>
      </el-upload>

      <el-button
        type="primary"
        :disabled="tableData.length > 0 ? false : true"
        @click="onSummaryAnalysis"
      >
        汇总分析
      </el-button>
    </div>

    <div class="main-container-mc">
      <el-table
        :data="
          tableData.slice(
            (pagination.currentPage - 1) * pagination.pageSize,
            pagination.currentPage * pagination.pageSize
          )
        "
        height="570"
        style="width: 100%"
      >
        <el-table-column prop="checkSubject" label="总账科目" sortable />
        <el-table-column prop="voucherNumber" label="凭证编号" sortable />
        <el-table-column prop="postingDate" label="过账日期" sortable />
        <el-table-column prop="contractNumber" label="合同号" sortable />
        <el-table-column
          prop="titleText"
          label="凭证抬头文本"
          width="330px"
          sortable
        />
        <el-table-column prop="principal" label="本币金额" sortable />
        <el-table-column
          prop="certificateType"
          label="凭证类型"
          width="120px"
          sortable
        >
          <template #default="scope">
            <span v-if="scope.row.certificateType === 'ZB'">
              冲销【{{ scope.row.certificateType }}】
            </span>
            <span v-else-if="scope.row.certificateType === 'SA'">
              付款【{{ scope.row.certificateType }}】
            </span>
            <span v-else-if="scope.row.certificateType === 'ZA'">
              挂账【{{ scope.row.certificateType }}】
            </span>
            <span v-else>
              {{ scope.row.certificateType }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="accountAllocation" label="分配" sortable />
        <el-table-column
          prop="assigner"
          label="分配人"
          width="120px"
          sortable
        />
      </el-table>

      <div style="width: 100%; height: 32px; margin: 40px 0 20px 0">
        <el-pagination
          background
          :page-sizes="[10, 20, 30, 100, 150, 200]"
          layout="total, prev, pager, next, sizes"
          :total="pagination.total"
          :current-page="pagination.currentPage"
          :page-size="pagination.pageSize"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          style="float: right"
        />
      </div>
    </div>
  </div>

  <!-- 模态框 -->
  <el-dialog
    draggable
    v-model="dialogVisible"
    title="汇总分析数据"
    width="60%"
    :close-on-click-modal="false"
  >
    <el-button type="primary" :icon="Download" @click="exportExcel">
      导出
    </el-button>
    <div class="dialogTable">
      <el-table border :data="tableCollect" style="width: 100%">
        <el-table-column prop="key0" label="合同号" width="180px" />
        <el-table-column prop="key1" label="船舶服务中心" />
        <el-table-column prop="key2" label="水下作业及测试部" />
        <el-table-column prop="key3" label="管缆作业部" />
        <el-table-column prop="key4" label="ROV作业部" />
        <el-table-column prop="key5" label="潜水作业部" />
        <el-table-column prop="key6" label="合同求和">
          <template #default="{ row, column }">
            <span :style="{ color: '#fff' }">{{ row[column.property] }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="dialogVisible = false">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import * as XLSX from "xlsx";
import { Download } from "@element-plus/icons-vue";

interface Data {
  checkSubject?: string;
  voucherNumber?: string;
  postingDate?: string;
  contractNumber?: string;
  titleText?: string;
  principal?: number;
  certificateType?: string;
  accountAllocation?: string;
  assigner?: string;
}

interface Collect {
  key0?: string;
  key1?: string;
  key2?: string;
  key3?: string;
  key4?: string;
  key5?: string;
  key6?: string;
}

interface Structure {
  section?: string;
  contract?: string;
  amount?: number;
}

const tableData = ref<Data[]>([]);
const tableCollect = ref<Collect[]>([]);
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});
const dialogVisible = ref<boolean>(false);
const excelName = ref<string>();

// 分配 部门
const section = reactive([
  "船舶服务中心",
  "水下作业及测试部",
  "管缆作业部",
  "ROV作业部",
  "潜水作业部",
]);

const keyMappings: any = reactive({
  contract: "key0",
  amount船舶服务中心: "key1",
  amount水下作业及测试部: "key2",
  amount管缆作业部: "key3",
  amountROV作业部: "key4",
  amount潜水作业部: "key5",
  key6: "key6",
});

// 把文件按照二进制进行读取
function readFile(file: any) {
  return new Promise((resolve) => {
    let reader = new FileReader();
    reader.readAsArrayBuffer(file);
    reader.onload = (ev) => {
      resolve(ev.target?.result);
    };
  });
}

// 分页
const handleSizeChange = (val: number) => {
  pagination.pageSize = val;
};
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current;
};

// 选择文件的方法
const handle = async (ev: any) => {
  tableData.value = [];
  let file = ev.raw; //这里面就是数据
  excelName.value = file.name.split(".")[0]; // 截取 导入excel名称
  let data: any = await readFile(file); // 将file变成二进制读取到的
  let workbook = XLSX.read(data, { type: "binary" }); // 将得到的二进制转化一下
  // 最后把数据转成json格式的
  let worksheet = workbook.Sheets[workbook.SheetNames[0]]; //这里是表格的名字,这里取第一个表格,1就是第二个表格数据
  data = XLSX.utils.sheet_to_json(worksheet);
  data.forEach((item: any) => {
    let obj: Data = {
      checkSubject: item["总账科目"],
      voucherNumber: item["凭证编号"],
      postingDate: item["过账日期"],
      contractNumber: item["合同号"],
      titleText: item["凭证抬头文本"],
      principal: item["本币金额"],
      certificateType: item["凭证类型"],
      accountAllocation: item["分配"],
      assigner: item["分配人"],
    };

    // 转换Excel时间格式
    const excelDates: any = obj.postingDate; // 示例数字数组
    // 转换函数
    // 这里减去25567是因为Excel以1900年1月1日为起点计算日期
    const jsDate = new Date((excelDates - 25567 - 2) * 24 * 3600 * 1000);
    const dateString = new Date(jsDate);
    const formattedDate = () => {
      const date = new Date(dateString);
      return `${date.getFullYear()}-${(date.getMonth() + 1)
        .toString()
        .padStart(2, "0")}-${date.getDate().toString().padStart(2, "0")}`;
    };
    obj.postingDate = formattedDate();

    pagination.total = tableData.value.length + 1;
    tableData.value.push(obj);
  });
};

//汇总分析
const onSummaryAnalysis = () => {
  const data = tableData.value;
  let contract = []; // 合同号
  for (let i in data) {
    if (contract.indexOf(data[i].contractNumber) == -1) {
      contract.push(data[i].contractNumber);
    }
  }

  // 初级 计算费用 && 重组数据结构
  const dataStructure: Structure[] = [];
  section.forEach((item1) => {
    contract.forEach((item2) => {
      let num = 0;
      for (let i in data) {
        if (
          data[i].accountAllocation === item1 &&
          data[i].contractNumber === item2
        ) {
          num = num + data[i].principal!;
          num = parseFloat(num.toFixed(2));
        }
      }
      let obj = {
        section: item1,
        contract: item2,
        amount: num,
      };
      dataStructure.push(obj);
    });
  });

  // 二次重组数据结构
  const listFun = dataStructure.reduce((acc: any, cur: any) => {
    if (!acc[cur.contract]) {
      acc[cur.contract] = {
        contract: cur.contract,
      };
    }
    acc[cur.contract][`amount${cur.section}`] = cur.amount;
    return acc;
  }, {});

  const list: any[] = [];
  contract.forEach((item: any) => {
    list.push(listFun[item]);
  });

  // 横向、纵向求和计算
  amountCompute(list, dataStructure);

  dialogVisible.value = true;
};

// 横向、纵向求和计算
const amountCompute = (list: any, dataStructure: Structure[]) => {
  const updatedArray = list.map((item: any) => {
    const newObject: any = {};
    Object.keys(item).forEach((key) => {
      if (keyMappings[key]) {
        newObject[keyMappings[key]] = item[key];
      }
    });
    return newObject;
  });

  // 横向 求和 计算
  const obj: any = {};
  const listNum = [];
  section.forEach((item) => {
    let num = 0;
    for (let i in dataStructure) {
      if (item === dataStructure[i].section) {
        if (isNaN(dataStructure[i].amount!)) {
          dataStructure[i].amount = 0;
        }
        num = num + dataStructure[i].amount!;
        num = parseFloat(num.toFixed(2));
      }
    }
    listNum.push(num);
  });
  listNum.unshift(null);

  listNum.forEach((item: null, index: number) => {
    obj["key" + index] = item;
  });
  updatedArray.push(obj);
  // 纵向求和计算
  for (let i in updatedArray) {
    updatedArray[i].key6 =
      updatedArray[i].key1 +
      updatedArray[i].key2 +
      updatedArray[i].key3 +
      updatedArray[i].key4 +
      updatedArray[i].key5;
    updatedArray[i].key6 = parseFloat(updatedArray[i].key6.toFixed(2));
  }
  tableCollect.value = updatedArray;
};

// 导出数据
const exportExcel = () => {
  let titleObj: any = {
    key0: "合同号",
    key1: "船舶服务中心",
    key2: "水下作业及测试部",
    key3: "管缆作业部",
    key4: "ROV作业部",
    key5: "潜水作业部",
    key6: "合同求和",
  };
  const tableData = XLSX.utils.json_to_sheet(tableCollect.value);
  const wb = XLSX.utils.book_new();

  // 把表格的表头替换为对应的汉字
  for (const key in tableData) {
    if (tableData.hasOwnProperty(key)) {
      if (titleObj[tableData[key].v]) {
        tableData[key].v = titleObj[tableData[key].v];
      }
    }
  }

  XLSX.utils.book_append_sheet(wb, tableData, "汇总分析表");
  XLSX.writeFile(wb, excelName.value + ".xlsx");
};
</script>

<style scoped lang="less">
.dialogTable {
  padding-top: 10px;

  ::v-deep(.el-table .el-table__body tr:last-child) {
    background-color: #ffd700;
  }
  ::v-deep(.el-table .el-table__body tr td:last-child) {
    background-color: #6495ed;
  }
  ::v-deep(.el-table .cell) {
    color: #696969;
  }
}
</style>
