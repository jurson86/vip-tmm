<template>
	<div>
		<div class="mrl">
			<Breadcrumb>
				<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
				<BreadcrumbItem :to="{name: 'applicationList'}">服务列表</BreadcrumbItem>
				<BreadcrumbItem>{{titleName}}</BreadcrumbItem>
			</Breadcrumb>
		</div>
		<Card :dis-hover="true" class="mrl">
			<p slot="title">{{titleName}}</p>
			<Form ref="form" :model="formData" :rules="formRules" :label-width="120" class="content-form">
				<FormItem label="服务名" prop="applicationName">
        			<Input v-model="formData.applicationName" placeholder="服务名"></Input>
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
					// applicationId: '',
					applicationName: ''
				},
				formRules: {
					applicationName: [
						{ required: true, message: '不能输入中文', pattern: /^[^\u4e00-\u9fa5]{0,}$/, trigger: 'blur' }  
					]
				}
			}
		},

		computed: {
			applicationId(){
				return this.$route.params.id;
			},

			titleName(){
				return this.applicationId ? '修改' : '新增';
			}
		},

		created(){
			if(this.applicationId){
				this.getDetail();
			}
		},

		methods: {
			submitForm(){
				const url = this.applicationId ? '/application/updateApplication' : '/application/saveApplication';

				this.$refs.form.validate((valid) => {
					if(valid){
						this.$post(url, this.formData).then(res => {
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

			getDetail(){
				this.$post('/application/getApplicationInfo', {
					applicationId: this.applicationId
				}).then(res => {
					if(res.code == 200){
						this.formData.applicationId = res.data.pid;
						this.formData.applicationName = res.data.applicationName;
					}
				})
			}
		}
	}
</script>