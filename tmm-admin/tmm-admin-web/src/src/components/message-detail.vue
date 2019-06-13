<template>
	<div class="expand">
		<p class="expand-title">消息详情</p>
		<p class="expand-content">{{message}}</p>
	</div>
</template>

<script type="es6">
	export default{
		props: {
			pid: Number
		},

		data(){
			return{
				message: ''
			}
		},

		created(){
			this.getMessageDetail();
		},

		methods: {
			getMessageDetail(){
				this.$get('/api/message/detail/pid?pid=' + this.pid).then(res => {
					if(res.status == 200){
						this.message = res.data.message;
					}
				})
			}
		}
	}
</script>