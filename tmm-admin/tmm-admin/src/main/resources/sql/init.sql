/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50527
Source Host           : localhost:3306
Source Database       : trans

Target Server Type    : MYSQL
Target Server Version : 50527
File Encoding         : 65001

Date: 2018-06-08 11:24:45
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `t_application`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_application` (
  `pid` int(11) NOT NULL AUTO_INCREMENT,
  `application_name` varchar(40) NOT NULL COMMENT '服务名',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`pid`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_application
-- ----------------------------



-- ----------------------------
-- Table structure for `t_role`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_role` (
  `pid` int(11) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(20) NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`pid`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_role
-- ----------------------------

-- ----------------------------
-- Table structure for `t_role_permission`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_role_permission` (
  `pid` int(11) NOT NULL AUTO_INCREMENT,
  `application_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  PRIMARY KEY (`pid`),
  UNIQUE KEY `role_permission_index` (`role_id`,`application_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_role_permission
-- ----------------------------

-- ----------------------------
-- Table structure for `t_role_user`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_role_user` (
  `pid` int(11) NOT NULL AUTO_INCREMENT,
  `role_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`pid`),
  UNIQUE KEY `role_user` (`role_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_role_user
-- ----------------------------




-- ----------------------------
-- Records of t_transaction_state
-- ----------------------------

-- ----------------------------
-- Table structure for `t_user`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_user` (
  `pid` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(20) NOT NULL,
  `password` varchar(255) NOT NULL,
  `status` tinyint(3) DEFAULT '1' COMMENT '是否删除 1：正常，2：删除',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`pid`)
) ENGINE=InnoDB AUTO_INCREMENT=132 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_user
-- ----------------------------
