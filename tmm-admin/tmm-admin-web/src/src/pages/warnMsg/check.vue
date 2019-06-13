<template>
	<div>
		<Breadcrumb class="mrl">
			<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
			<BreadcrumbItem :to="{name: 'warnMsg'}">异常消息</BreadcrumbItem>
			<BreadcrumbItem>check</BreadcrumbItem>
		</Breadcrumb>
		<Card :dis-hover="true" class="mrl">
			<p slot="title">check</p>
			<Form ref="form" :model="formData" :label-width="120" class="content-form">
				<FormItem label="message">
					<Input v-model="formData.message" placeholder="请输入"></Input>
				</FormItem>
				<FormItem label="topic">
					<Card :dis-hover="true">
						<div class="child-info">
							<span>vHost</span>
							<Input v-model="formData.topic.vHost" placeholder="请输入"></Input>
						</div>
						<div class="child-info">
							<span>exchange</span>
							<Input v-model="formData.topic.exchange" placeholder="请输入"></Input>
						</div>
						<div class="child-info">
							<span>exchangeType</span>
							<Select v-model="formData.topic.exchangeType">
								<Option value="fanout"></Option>
								<Option value="direct"></Option>
								<Option value="topic"></Option>
								<Option value="headers"></Option>
							</Select>
						</div>
						<div class="child-info">
							<span>routeKey</span>
							<Input v-model="formData.topic.routeKey" placeholder="请输入"></Input>
						</div>
						<div class="child-info">
							<span>customExchange</span>
							<Checkbox v-model="formData.topic.customExchange">是否消费者手动创建（false为默认创建）</Checkbox>
						</div>
						<div class="child-info">
							<span>queue</span>
							<Input v-model="formData.topic.queue" placeholder="请输入"></Input>
						</div>
					</Card>
				</FormItem>
				<FormItem label="state">
					<Select v-model="formData.state">
						<Option :value="0">提交</Option>
						<Option :value="1">取消</Option>
					</Select>
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
					message: '',
					topic: {
						vHost: '',
						exchange: '',
						exchangeType: '',     // fanout, direct, topic, headers
						routeKey: '',
						customExchange: true,     // false默认创建，true消费者手动创建
						queue: ''
					},
					state: 0         // 0提交，1取消
				},
				formRules: {}
			}
		},

		computed: {
			pids(){
				return this.$route.query.ids;
			}
		},

		created(){},

		methods: {
			submitForm(){
				this.$post('/api/message/completion', {
					message: this.formData.message,
					topic: JSON.stringify(this.formData.topic),
					state: this.formData.state,
					pids: this.pids
				}).then(res => {
					if(res.code == 200){
						this.$Modal.success({
    						title: '提示',
    						content: '补全check消息成功',
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

<style lang="scss" scoped>
	.child-info{
		margin-bottom: 10px;

		span{
			display: inline-block;
			width: 95px;
			font-size: 12px;
		}

		.ivu-input-wrapper, .ivu-select{
			width: 70%;
		}
	}
</style>