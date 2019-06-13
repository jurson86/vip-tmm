<template>
	<div>
		<div class="mrl">
			<Breadcrumb>
				<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
				<BreadcrumbItem>角色列表</BreadcrumbItem>
			</Breadcrumb>
		</div>
		<Row class="button-box">
			<Col span="4">
				<Input v-model="params.roleName" placeholder="角色名"></Input>
			</Col>
			<Col span="8">
				<Button type="ghost" class="mr" @click="searchData">查询</Button>
				<Button type="info" class="mr" @click="addItem">新增</Button>
				<!-- <Button type="warning" class="mr" @click="editItem">授权</Button> -->
				<Button type="error" @click="delItem">删除</Button>
			</Col>
		</Row>
		<Table class="mrl" border stripe :loading="loading" :columns="columns" :data="tableData" @on-selection-change="getSelection"></Table>
		<Page class="page" :total="totalCount" :current="params.page" :page-size="params.pageSize" show-total @on-change="changePage"></Page>
	</div>
</template>

<script type="es6">
	export default{
		data(){
			return{
				loading: false,
				params: {
					roleName: '',
					page: 1,
					pageSize: 20
				},
				totalCount: 0,
				selectionArr: [],
				tableData: [],
				columns: [
					{
						type: 'selection',
						width: 60,
						aligin: 'center'
					},
					{
						title: 'roleName',
						key: 'roleName'
					},
					{
						title: 'createTime',
						key: 'createTime'
					},
					{
						title: 'updateTime',
						key: 'updateTime'
					},
					{
						title: '',
						key: 'actions',
						render: (h, params) => {
							return h('Button', {
								props: {
									size: 'small',
									type: 'primary'
								},
								on: {
									click: () => {
										this.editItem(params.row.pid);
									}
								}
							}, '授权')
						}
					}
				]
			}
		},

		created(){
			this.getTableData();
		},

		methods: {
			searchData(){
				this.params.page = 1;
				this.getTableData();
			},

			addItem(){
				this.$router.push({name: 'roleAdd'});
			},

			editItem(id){
				// if(this.selectionArr.length === 0 || this.selectionArr.length > 1){
				// 	this.$Message.warning('请选择一条数据！');
				// 	return;
				// }

				this.$router.push({
					name: 'roleEdit',
					params: {
						id: id
					}
				})
			},

			delItem(){
				if(this.selectionArr.length === 0){
					this.$Message.warning('请至少选择一条数据！');
					return;
				}

				let array = [];
				this.selectionArr.map(item => {
					array.push(item.pid);
				});

				this.$Modal.confirm({
					title: '删除提示',
					content: '确认删除这些数据吗？',
					onOk: () => {
						this.$post('/role/delRole', {roleIds: array}).then(res => {
							if(res.code == 200){
								this.$Message.success('删除成功！');
								this.getTableData();
							}else{
								this.$Message.error(res.msg);
							}
						})
					}
				})
			},

			getSelection(selection){
				this.selectionArr = selection;
			},

			changePage(cur){
				this.params.page = cur;
				this.getTableData();
			},

			getTableData(){
				this.$post('/role/queryRolelist', this.params).then(res => {
					if(res.code == 200){
						this.tableData = res.data.content;
						this.totalCount = res.data.total;
					}
				})
			}
		}
	}
</script>