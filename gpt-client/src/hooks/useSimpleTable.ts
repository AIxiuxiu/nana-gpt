import { reactive } from 'vue';
/**
 * 简单管理表格
 */
export default function useSimpleTable() {
  // list列表数据  selects选择数据
  const data = reactive({ show: false, editData: undefined, list: [] as any[], selects: [] as any[] });
  //添加数据
  function addData(value) {
    if (Object.prototype.hasOwnProperty.call(value, 'index')) {
      data.list[value.index] = value;
    } else {
      value.index = data.list.length;
      data.list.push(value);
    }
  }
  // 编辑数据
  function editData(value) {
    data.editData = value;
    data.show = true;
  }

  // 展示
  function show() {
    data.editData = undefined;
    data.show = true;
  }

  // 删除数据
  function deleteData(index) {
    ElMessageBox.confirm('确定删除吗？', '提示', {
      type: 'warning'
    }).then(() => {
      data.list.splice(index, 1);
    });
  }

  // 选择数据
  function selectChange(e) {
    data.selects = e.map((item) => item.index);
  }

  // 批量删除数据
  function deleteMultiple() {
    if (data.selects.length) {
      ElMessageBox.confirm('确定要删除吗？', '提示', {
        type: 'warning'
      }).then(() => {
        let i = 0;
        data.list = data.list.filter((item) => {
          if (!data.selects.includes(item.index)) {
            item.index = i;
            i++;
            return item;
          }
        });
      });
    } else {
      ElMessage.warning('请选择要删除的数据！');
    }
  }

  return { data, addData, deleteData, selectChange, deleteMultiple, editData, show };
}
