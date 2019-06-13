<template>
	<div class="backstage">
		<TopBar></TopBar>
		<Menu></Menu>
		<Scroll class="container">
			<router-view></router-view>
		</Scroll>
	</div>
</template>

<script type="es6">
	import TopBar from '../components/TopBar.vue';
	import Menu from '../components/menu.vue';

	export default{
		components: {
			TopBar, Menu
		}
	}
</script>