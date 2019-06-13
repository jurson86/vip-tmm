<template>
	<div>
		<Breadcrumb class="mrl">
			<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
			<BreadcrumbItem :to="{name: 'roleList'}">角色列表</BreadcrumbItem>
			<BreadcrumbItem>{{titleName}}</BreadcrumbItem>
		</Breadcrumb>
		<Card :dis-hover="true" class="mrl">
			<p slot="title">{{titleName}}</p>
			<Form v-if="!roleId" ref="form" :model="formData" :rules="formRules" :label-width="120" class="content-form">
				<FormItem label="角色名称" prop="roleName">
        			<Input v-model="formData.roleName" :maxlength="20" placeholder="角色名称"></Input>
        		</FormItem>
        		<FormItem> 
        			<Button type="primary" class="mr" @click="submitForm">确定</Button>
        			<Button type="ghost" @click="$router.go(-1)">取消</Button>
        		</FormItem>
			</Form>
			<Form v-if="roleId" :label-width="120" class="content-form">
				<FormItem label="角色权限列表">
					<CheckboxGroup v-model="applicationIds">
						<Row v-for="item in applicationList" :key="item.pid">
							<Checkbox :label="item.pid">{{item.applicationName}}</Checkbox>
						</Row>
					</CheckboxGroup>
				</FormItem>
				<FormItem v-if="applicationList.length !== 0"> 
        			<Button type="primary" class="mr" @click="addRolePermission">确定</Button>
        			<Button type="ghost" @click="$router.go(-1)">取消</Button>
        		</FormItem>
			</Form>
		</Card>
	</div>
</template>

<script type="es6">
	export default{
		data(){
			return{
				formData: {
					roleName: ''
				},
				formRules: {
					roleName: [
						{ required: true, message: '必填项', trigger: 'blur' }
					]
				},
				applicationIds: [],
				applicationList: []
			}
		},

		computed: {
			roleId(){
				return this.$route.params.id;
			},

			titleName(){
				return this.roleId ? '授权' : '新增';
			}
		},

		created(){
			if(this.roleId){
				this.rolePermissionList();
			}
		},

		methods: {
			submitForm(){
				this.$refs.form.validate((valid) => {
					if(valid){
						this.$post('/role/saveRole', this.formData).then(res => {
							if(res.code == 200){
								this.$Modal.success({
	        						title: '提示',
	        						content: this.titleName + '成功',
	        						onOk: () => {
	        							this.$router.go(-1);
	        						}
	        					})
							}else{
	        					this.$Modal.warning({
	        						title: '提示',
	        						content: res.msg
	        					})
	        				}
						})
					}else{
	        			this.$Message.warning('缺少必填或所填信息有误！');
	        		}
				})
			},

			rolePermissionList(){
				this.$post('/role/rolePermissionList', {
					roleId: this.roleId
				}).then(res => {
					if(res.code == 200){
						this.applicationList = res.data;
						for(let i = 0; i < res.data.length; i++){
							if(res.data[i].check){
								this.applicationIds.push(res.data[i].pid);
							}
						}
					}else{
						this.$Message.warning(res.msg);
					}
				})
			},

			addRolePermission(){
				this.$post('/role/addRolePermission', {
					roleId: this.roleId,
					applicationIds: this.applicationIds
				}).then(res => {
					if(res.code == 200){
						this.$Modal.success({
    						title: '提示',
    						content: this.titleName + '修改成功',
    						onOk: () => {
    							this.$router.go(-1);
    						}
    					})
					}else{
    					this.$Modal.warning({
    						title: '提示',
    						content: res.msg
    					})
    				}
				})
			}
		}
	}
</script>