from email.header import Header
from email.mime.text import MIMEText
from email.utils import parseaddr, formataddr
from email.mime.application import MIMEApplication
import smtplib
import os,platform
from config.config import EMAIL_RECEIVER
from email.mime.multipart import MIMEMultipart

def _format_addr(s):
    name, addr = parseaddr(s)
    return formataddr((name,  addr))

## ==============定义发送附件邮件==========
def send_file(file_new):
    smtpserver = 'smtp.exmail.qq.com'
    user = 'fengpengyong@tuandai.com'
    password = 'Fpengyong3031150'
    sender = 'fengpengyong@tuandai.com'
    receiver = EMAIL_RECEIVER
    file = open(file_new, 'r').read()

    msgRoot = MIMEMultipart('related')

    msg = MIMEText(file, 'html', 'utf-8')

    msgRoot['Subject'] = Header(u'tmm-test 集成测试报告', 'utf-8').encode()
    msgRoot['From'] = _format_addr(u'tmm-test测试组 <%s>' % sender)
    msgRoot['To'] = _format_addr(u'tmm 开发组 <%s>' % receiver)

    ## 设置附件头
    att = MIMEApplication(open(file_new, 'rb').read())
    att.add_header('Content-Disposition', 'attachment', filename="detail.html")
    msgRoot.attach(att)
    msgRoot.attach(msg)


    smtp = smtplib.SMTP(smtpserver, 25)
    smtp.login(user, password)
    smtp.sendmail(sender, receiver, msgRoot .as_string())
    smtp.quit()


# ======查找测试目录，找到最新生成的测试报告文件======
def new_report(test_report):
    lists = os.listdir(test_report)  # 列出目录的下所有文件和文件夹保存到lists
    lists.remove("HTMLReport.html")
    if (platform.system() == 'Windows'):
        lists.sort(key=lambda fn: os.path.getmtime(test_report + "\\" + fn))  # 按时间排序 win
    else:
        lists.sort(key=lambda fn: os.path.getmtime(test_report + "/" + fn)) #linux
    file_new = os.path.join(test_report, lists[-1])  # 获取最新的文件保存到file_new
    return file_new


if __name__ == "__main__":
    path = os.path.dirname(os.path.realpath(__file__)) + "\\test_report"
    file_new = new_report(path)
    send_file(file_new)
