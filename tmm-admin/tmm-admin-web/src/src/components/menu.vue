<template>
	<Scroll class="menu">
		<Menu theme="dark" :active-name="activeMenu" @on-select="linkUrl">
			<MenuItem name="home">MQ管理端</MenuItem>
			<MenuItem v-if="show" name="userList">用户列表</MenuItem>
			<MenuItem v-if="show" name="roleList">角色列表</MenuItem>
			<MenuItem name="applicationList">服务列表</MenuItem>
			<MenuItem name="mqList">RabbitMQ列表</MenuItem>
		</Menu>
	</Scroll>
</template>

<script type="es6">
	import {mapGetters} from 'vuex'

	export default{
		data(){
			return{
				activeMenu: 'home',
				show: false
			}
		},

		computed: {
            ...mapGetters([
                'userinfo'
            ])
        },

		created(){
			const activeMenu = sessionStorage.getItem('activeMenu');
			if(activeMenu){
				this.activeMenu = activeMenu;
			}

			if(this.userinfo.roleSet.length !== 0){
				for(let item of this.userinfo.roleSet){
					if(item == 'admin'){
						this.show = true;
					}
				}
			}
		},

		methods: {
			linkUrl(name){
				sessionStorage.setItem('activeMenu', name);
				this.$router.push({name: name});				
			}
		}
	}
</script>