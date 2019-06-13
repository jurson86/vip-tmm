<template>
	<div>
		<div class="button-box">
			<Button type="warning" class="mr" @click="warnMsg">异常消息</Button>
			<Button type="error" class="mr" @click="deathMsg">死亡消息</Button>
			<Button type="info" class="mr" @click="dieMsg">死信消息</Button>
			<Button type="primary" class="mr" @click="clearMsg">数据清理</Button>
			<Button class="mr" @click="serviceMsg">服务列表</Button>
			<a @click="linkHelp">TMM帮助</a>
		</div>
		<div class="img-box"></div>
	</div>
</template>

<script type="es6">
	export default{
		methods: {
			warnMsg(){
				this.$router.push({name: 'warnMsg'});
			},

			deathMsg(){
				this.$router.push({name: 'deathMsg'});
			},

			dieMsg(){
				this.$router.push({name: 'dieMsg'});
			},

			clearMsg(){
				this.$router.push({name: 'clearMsg'});
			},

			linkHelp(){
				this.$router.push({name: 'helpCenter'});
			},

			serviceMsg(){
				this.$router.push({name: 'serviceList'});
			}
		}
	}
</script>

<style lang="scss">
	.img-box{
		margin: 0 auto;
		height: 1049px;
		background: url('../assets/imgs/tmm.png') no-repeat center;
	}
</style>


