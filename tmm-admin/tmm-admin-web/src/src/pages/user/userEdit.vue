<template>
	<div>
		<div class="mrl">
			<Breadcrumb>
				<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
				<BreadcrumbItem :to="{name: 'userList'}">用户列表</BreadcrumbItem>
				<BreadcrumbItem>新增</BreadcrumbItem>
			</Breadcrumb>
		</div>
		<Card :dis-hover="true" class="mrl">
			<p slot="title">新增</p>
			<Form ref="form" :model="formData" :rules="formRules" :label-width="120" class="content-form">
				<FormItem label="用户名称" prop="userName">
        			<Input v-model="formData.userName" :maxlength="20" placeholder="用户名称"></Input>
        		</FormItem>
        		<FormItem label="密码" prop="passWord">
        			<Input v-model="formData.passWord" placeholder="密码"></Input>
        		</FormItem>
        		<FormItem> 
        			<Button type="primary" class="mr" @click="submitForm">确定</Button>
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
					userName: '',
					passWord: ''
				},
				formRules: {
					userName: [
						{ required: true, message: '必填项', trigger: 'blur' }
					],
					passWord: [
						{ required: true, message: '必填项', trigger: 'blur' },
						{
                            type: 'string',
                            min: 3,
                            message: '密码长度不能小于3位',
                            trigger: 'blur'
                        }
					]
				}
			}
		},

		computed: {},

		created(){},

		methods: {
			submitForm(){
				this.$refs.form.validate((valid) => {
					if(valid){
						this.$post('/user/saveUser', this.formData).then(res => {
							if(res.code == 200){
								this.$Modal.success({
	        						title: '提示',
	        						content: '新增成功',
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
			}
		}
	}
</script>