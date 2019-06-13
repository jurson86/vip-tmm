<template>
	<div class="header">
		<h3 class="title">TMM系统</h3>
		<p class="userinfo">欢迎您，{{userinfo.userName}}</p>
		<div class="actions">
			<!-- <a class="reset" @click=""><Icon type="ios-locked-outline"></Icon> 重置密码</a> -->
			<a class="logout" @click="toLogout"><Icon type="log-out"></Icon> 退出登录</a>
		</div>
	</div>
</template>

<script type="es6">
	import {mapMutations, mapGetters} from 'vuex'
	export default{
		data(){
			return{}
		},

		computed: {
            ...mapGetters([
                'userinfo'
            ])
        },

        methods: {
        	...mapMutations({
        		logout: 'LOGIN_OUT'
        	}),

        	toLogout(){
        		this.$Modal.confirm({
        			title: '提示',
        			content: '确认退出吗？',
        			onOk: () => {
        				this.$post('/logout').then(res => {
		        			this.logout();
		        			this.$router.push({name: 'login'});
		        		})
        			}
        		})
        	}
        }
	}
</script>