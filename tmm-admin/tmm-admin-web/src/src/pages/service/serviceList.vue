<template>
	<div>
		<div class="mrl">
			<Breadcrumb>
				<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
				<BreadcrumbItem>服务列表</BreadcrumbItem>
			</Breadcrumb>
		</div>
		<Row class="button-box">
			<Col span="4">
				<Input v-model="params.applicationName" placeholder="服务名"></Input>
			</Col>
			<Col span="12">
				<Button type="ghost" class="mr" @click="searchData">查询</Button>
				<Button type="info" class="mr" @click="addItem">新增</Button>
				<Button type="warning" class="mr" @click="editItem">修改</Button>
				<Button type="error" class="mr" @click="delItem">删除</Button>
				<Button type="primary" @click="openModal">绑定队列和服务</Button>
			</Col>
		</Row>
		<Table class="mrl" border stripe :loading="loading" :columns="columns" :data="tableData" @on-selection-change="getSelection"></Table>
		<Page class="page" :total="totalCount" :current="params.page" :page-size="params.pageSize" show-total @on-change="changePage"></Page>

		<Modal v-model="modal" title="绑定队列和服务的关系" @on-ok="bindDlqName" @on-cancel="closeModal">
			<Form :label-width="100" ref="form" :model="formData">
				<FormItem label="服务名">
					<Input v-model="formData.serviceName" disabled></Input>
				</FormItem>
				<FormItem label="队列名" prop="dlqName" :rules="{required: true, message: '不能输入中文', pattern: /^[^\u4e00-\u9fa5]{0,}$/, trigger: 'blur'}">
					<Input v-model="formData.dlqName" placeholder="请输入队列名"></Input>
				</FormItem>
			</Form>
		</Modal>
	</div>
</template>

<script type="es6">
	export default{
		data(){
			return{
				loading: false,
				modal: false,
				formData: {
					serviceName: '',
					dlqName: ''
				},
				params: {
					applicationName: '',
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
						title: 'applicationName',
						key: 'applicationName'
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
										this.$router.push({
											name: 'serviceQueList',
											params: {
												name: params.row.applicationName
											}
										})
									}
								}
							}, '查看服务列表队列')
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
				this.$router.push({name: 'applicationAdd'});
			},

			editItem(){
				if(this.selectionArr.length === 0 || this.selectionArr.length > 1){
					this.$Message.warning('请选择一条数据！');
					return;
				}

				this.$router.push({
					name: 'applicationEdit',
					params: {
						id: this.selectionArr[0].pid
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
						this.$post('/application/delApplication', {applicationIds: array}).then(res => {
							if(res.code == 200){
								this.$Message.success('删除成功！');
								this.getTableData();
								this.selectionArr = [];
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
				this.loading = true;
				this.$post('/application/queryApplicationlist', this.params).then(res => {
					this.loading = false;
					
					if(res.code == 200){
						this.tableData = res.data.content;
						this.totalCount = res.data.total;
					}
				})
			},

			openModal(){
				if(this.selectionArr.length === 0 || this.selectionArr.length > 1){
					this.$Message.warning('请选择一条数据！');
					return;
				}

				this.modal = true;
				this.formData.serviceName = this.selectionArr[0].applicationName;
			},

			closeModal(){
				this.formData.dlqName = '';
			},

			bindDlqName(){
				this.$get('/api/add/dlq/service', this.formData).then(res => {
					if(res.status == 200){
						this.$Message.success('绑定成功！');
						this.getTableData();
						this.formData.dlqName = '';
						this.selectionArr = [];
					}else{
						this.$Message.warning(res.msg);
					}
				})
			}
		}
	}
</script>