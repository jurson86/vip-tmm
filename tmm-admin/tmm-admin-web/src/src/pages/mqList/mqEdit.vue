<template>
	<div>
		<div class="mrl">
			<Breadcrumb>
				<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
				<BreadcrumbItem :to="{name: 'mqList'}">RabbitMQ列表</BreadcrumbItem>
				<BreadcrumbItem>{{titleName}}</BreadcrumbItem>
			</Breadcrumb>
		</div>
		<Card :dis-hover="true" class="mrl">
			<p slot="title">{{titleName}}</p>
			<Form ref="form" :model="formData" :label-width="120" class="content-form">
				<FormItem label="userName">
					<Input v-model="formData.userName"></Input>
				</FormItem>
				<FormItem label="passWord">
					<Input v-model="formData.passWord"></Input>
				</FormItem>
				<FormItem label="adminUrl">
					<Input v-model="formData.adminUrl"></Input>
				</FormItem>
				<FormItem label="host">
					<Input v-model="formData.host"></Input>
				</FormItem>
				<FormItem label="port">
					<Input v-model="formData.port"></Input>
				</FormItem>
				<FormItem label="mqKey">
					<Input v-model="formData.mqKey"></Input>
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
					mqKey: '',
					host: '',
					port: '',
					userName: '',
					passWord: '',
					adminUrl: ''
				}
			}
		},

		computed: {
			itemId(){
				return this.$route.params.id;
			},

			titleName(){
				return this.itemId ? '修改' : '新增';
			}
		},

		created(){
			if(this.itemId)
				this.getDetail();
		},

		methods: {
			submitForm(){
				const url = this.itemId ? '/mq/update' : '/mq/add';

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
			},

			getDetail(){
				this.$get('mq/query/details?pid=' + this.itemId).then(res => {
					if(res.code == 200){
						for(let key in this.formData){
							this.formData[key] = res.data[key];
						}
						this.formData.pid = this.itemId;
					}else{
						this.$Message.warning(res.msg);
					}
				})
			}
		}
	}
</script>