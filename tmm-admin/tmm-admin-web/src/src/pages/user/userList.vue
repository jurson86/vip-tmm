<template>
	<div>
		<div class="mrl">
			<Breadcrumb>
				<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
				<BreadcrumbItem>用户列表</BreadcrumbItem>
			</Breadcrumb>
		</div>
		<Row class="button-box">
			<Col span="4">
				<Input v-model="params.userName" placeholder="用户名"></Input>
			</Col>
			<Col span="4">
				<Select v-model="params.status" @on-change="searchData">
					<Option v-for="item in status" :value="item.id" :key="item.id">{{item.name}}</Option>
				</Select>
			</Col>
			<Col span="8">
				<Button type="ghost" class="mr" @click="searchData">查询</Button>
				<Button type="info" class="mr" @click="addItem">新增</Button>
				<Button type="error" @click="delItem">删除</Button>
			</Col>
		</Row>
		<Table class="mrl" border stripe :loading="loading" :columns="columns" :data="tableData" @on-selection-change="getSelection"></Table>
		<Page class="page" :total="totalCount" :current="params.page" :page-size="params.pageSize" show-total @on-change="changePage"></Page>

		<Modal v-model="modal" title="重置密码" @on-ok="resetPwd" @on-cancel="closeModal">
			<Form :label-width="100" ref="form" :model="formData">
				<FormItem label="用户名">
					<Input v-model="username" disabled></Input>
				</FormItem>
				<FormItem label="新密码" prop="passWord" :rules="{required: true, message: '密码最小长度为3位', min: 3, trigger: 'blur'}">
					<Input v-model="formData.passWord" type="password" placeholder="请输入新密码"></Input>
				</FormItem>
			</Form>
		</Modal>

		<Modal v-model="userRoleModal" title="用户角色列表" @on-ok="addUserRole" @on-cancel="closeUserRoleModal">
			<CheckboxGroup v-model="userParams.roleIds">
				<Row v-for="item in userRoleList" :key="item.pid">
					<Checkbox :label="item.pid">{{item.roleName}}</Checkbox>
				</Row>
			</CheckboxGroup>
		</Modal>
	</div>
</template>

<script type="es6">
	export default{
		data(){
			return{
				loading: false,
				modal: false,
				userRoleModal: false,
				username: '',
				formData: {
					userId: '',
					passWord: ''
				},
				userRoleList: [],
				userParams: {
					roleIds: [],
					userId: ''
				},
				params: {
					userName: '',
					status: 1,
					page: 1,
					pageSize: 20
				},
				status: [
					{id: 1, name: '正常'},
					{id: 0, name: '冻结'}
				],
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
						title: 'userName',
						key: 'userName'
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
						title: 'status',
						key: 'status',
						render: (h, params) => {
							return h('Select', {
	                			props: {
	                				value: params.row.status
	                			},
	                			on: {
	                				'on-change': (val) => {
	                					this.$Modal.confirm({
	                						title: '状态变更提示',
	                						content: '确认修改吗？',
	                						onOk: () => {
	                							this.$post('/user/updateUser', {
			                						userId: params.row.pid,
			                						userName: params.row.userName,
			                						status: val
			                					}).then(res => {
			                						if(res.code == 200){
														this.$Message.success('状态更新成功！');
														this.getTableData();
													}else{
														this.$Message.warning(res.msg);
														this.getTableData();
													}
			                					})
	                						},
	                						onCancel: () => {
	                							this.getTableData();
	                						}
	                					})
	                				}
	                			}
	                		}, [
	                			h('Option', {
	                				props: {
	                					value: 1
	                				}
	                			}, '正常'),
	                			h('Option', {
	                				props: {
	                					value: 0
	                				}
	                			}, '冻结')
	                		])
						}
					},
					{
						title: '',
						key: 'actions',
						width: 200,
						render: (h, params) => {
							return h('div', [
								h('Button', {
									style: {
										'margin-right': '10px'
									},
									props: {
										size: 'small',
										type: 'warning'
									},
									on: {
										click: () => {
											this.modal = true;
											this.formData.userId = params.row.pid;
											this.username = params.row.userName;
										}
									}
								}, '重置密码'),
								h('Button', {
									props: {
										size: 'small',
										type: 'success'
									},
									on: {
										click: () => {
											this.userRoleModal = true;
											this.userParams.userId = params.row.pid;
											this.$post('/user/userRoleList', {
												userId: params.row.pid
											}).then(res => {
												if(res.code == 200){
													this.userRoleList = res.data;
													for(let i = 0; i < res.data.length; i++){
														if(res.data[i].check){
															this.userParams.roleIds.push(res.data[i].pid);
														}
													}
												}else{
													this.$Message.warning(res.msg);
												}
											})
										}
									}
								}, '分配用户角色')
							])
						}
					}
				]
			}
		},

		created(){
			this.getTableData();
		},

		methods: {
			getSelection(selection){
				this.selectionArr = selection;
			},

			changePage(cur){
				this.params.page = cur;
				this.getTableData();
			},

			getTableData(){
				this.$post('/user/queryUserlist', this.params).then(res => {
					if(res.code == 200){
						this.tableData = res.data.content;
						this.totalCount = res.data.total;
					}
				})
			},

			searchData(){
				this.params.page = 1;
				this.getTableData();
			},

			addItem(){
				this.$router.push({name: 'userAdd'});
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
						this.$post('/user/delUser', {userIds: array}).then(res => {
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

			resetPwd(){
				this.$refs.form.validate((valid) => {
					if(valid){
						this.$post('/user/restUserPassWord', this.formData).then(res => {
							if(res.code == 200){
								this.$Message.success('重置密码成功！');
								this.getTableData();
								this.formData.userId = '';
								this.formData.passWord = '';
							}else{
								this.$Message.warning(res.msg);
							}
						})
					}
				})
			},

			closeModal(){
				this.formData.userId = '';
				this.formData.passWord = '';
				this.username = '';
			},

			closeUserRoleModal(){
				this.userRoleModal = false;
				this.userParams.roleIds = [];
			},

			addUserRole(){
				this.$post('/user/addUserRole', this.userParams).then(res => {
					if(res.code == 200){
						this.$Message.success('用户角色修改成功！');
						this.userRoleModal = false;
						this.userParams.roleIds = [];
						this.userParams.userId = '';
					}else{
						this.$Message.warning(res.msg);
					}
				})
			}
		}
	}
</script>