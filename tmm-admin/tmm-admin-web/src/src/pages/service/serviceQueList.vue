<template>
	<div>
		<Breadcrumb class="mrl">
			<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
			<BreadcrumbItem :to="{name: 'applicationList'}">服务列表</BreadcrumbItem>
			<BreadcrumbItem>服务绑定队列列表</BreadcrumbItem>
		</Breadcrumb>
		<Table class="mrl" border stripe :loading="loading" :columns="columns" :data="tableData"></Table>
	</div>
</template>

<script type="es6">
	export default{
		data(){
			return{
				loading: false,
				tableData: [],
				columns: [
					{
                        type: 'index',
                        width: 60,
                        align: 'center'
                    },
					{
						title: 'serviceName',
						key: 'serviceName',
					},
					{
						title: 'dlqName',
						key: 'dlqName'
					},
					{
						title: 'createTime',
						key: 'createTime',
					},
					{
						title: 'updateTime',
						key: 'updateTime',
					},
					{
						title: '',
						key: 'actions',
						render: (h, params) => {
							return h('Button', {
								props: {
									size: 'small',
									type: 'primary'
								},
								on: {
									click: () => {
										this.$Modal.confirm({
											content: '确定删除吗？',
											title: '提示',
											onOk: () => {
												this.$get('/api/dlq/delServiceQue?pid=' + params.row.pid).then(res => {
													if(res.status == 200){
														this.$Message.success('删除成功！');
														this.getTableData();
													}else{
														this.$Message.warning(res.msg);
													}
												})
											}
										})
									}
								}
							}, '删除')
						}
					}
				]
			}
		},

		created(){
			this.getTableData();
		},

		computed: {
			serviceName(){
				return this.$route.params.name;
			}
		},

		methods: {
			getTableData(){
				this.loading = true;
				this.$get('/api/dlq/serviceQueList', {
					serviceName: this.serviceName
				}).then(res => {
					this.loading = false;
					if(res.status == 200){
						this.tableData = res.data;
					}
				})
			}
		}
	}
</script>