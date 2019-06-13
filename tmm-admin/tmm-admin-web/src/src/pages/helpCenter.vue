<template>
	<div>
		<div class="mrl">
			<Breadcrumb>
				<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
				<BreadcrumbItem :to="{name: 'helpCenter'}">帮助中心</BreadcrumbItem>
			</Breadcrumb>
		</div>
		<div class="mrl">
			<vue-markdown>{{msg}}</vue-markdown>
		</div>
	</div>
</template>

<script type="es6">
	import HelpText from '../markdown/version.md';
	import VueMarkdown from 'vue-markdown';

	export default{
		data(){
			return{
				msg: HelpText
			}
		},

		components: {
			VueMarkdown
		}
	}
</script>