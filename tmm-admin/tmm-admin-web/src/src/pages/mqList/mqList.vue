<template>
	<div>
		<div class="mrl">
			<Breadcrumb>
				<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
				<BreadcrumbItem>RabbitMQ列表</BreadcrumbItem>
			</Breadcrumb>
		</div>
		<Row class="button-box">
			<Col span="4">
				<Button type="info" class="mr" @click="addItem">新增</Button>
			</Col>
		</Row>
		<Table class="mrl" border stripe :loading="loading" :columns="columns" :data="tableData"></Table>
		<Page class="page" :total="totalCount" :current="params.page" :page-size="params.pageSize" show-total @on-change="changePage"></Page>
	</div>
</template>

<script type="es6">
	export default{
		data(){
			return{
				loading: false,
				totalCount: 0,
				params: {
					page: 1,
					pageSize: 20
				},
				tableData: [],
				columns: [
					{
						type: 'index',
						width: 60,
						align: 'center'
					},
					{
						title: 'userName',
						key: 'userName'
					},
					{
						title: 'passWord',
						key: 'passWord'
					},
					{
						title: 'adminUrl',
						key: 'adminUrl'
					},
					{
						title: 'host',
						key: 'host'
					},
					{
						title: 'port',
						key: 'port'
					},
					{
						title: 'mqKey',
						key: 'mqKey'
					},
					{
						title: 'createTime',
						key: 'createTime'
					},
					{
						title: 'updateTime',
						key: 'updateTime'
					},
					{
						width: 150,
						render: (h, params) =>{
							return h('div', [
								h('Button', {
									style: {
										'margin-right': '10px'
									},
									props: {
										type: 'primary',
										size: 'small'
									},
									on: {
										click: () => {
											this.$router.push({
												name: 'mqEdit',
												params: {
													id: params.row.pid
												}
											})
										}
									}
								}, '修改'),
								h('Button', {
									props: {
										type: 'warning',
										size: 'small'
									},
									on: {
										click: () => {
											this.$Modal.confirm({
												title: '删除提示',
												content: '确认删除吗？',
												onOk: () => {
													this.$post('/mq/delete?pids=' + params.row.pid).then(res => {
														if(res.code == 200){
															this.$Message.success('删除成功！');
															this.getTableData();
														}else{
															this.$Message.error(res.msg);
														}
													})
												}
											})
										}
									}
								}, '删除')
							])
						}
					}
				]				
			}
		},

		created(){
			this.getTableData();
		},

		methods: {
			changePage(cur){},

			getTableData(){
				this.loading = true;
				this.$get('/mq/query/list').then(res => {
					this.loading = false;
					if(res.code == 200){
						this.tableData = res.data;
						this.totalCount = res.data.length;						
					}else{
						this.$Message.warning(res.msg);
					}
				})
			},

			addItem(){
				this.$router.push({name: 'mqAdd'});
			}
		}
	}
</script>