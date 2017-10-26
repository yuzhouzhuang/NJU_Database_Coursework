/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50621
Source Host           : localhost:3306
Source Database       : test

Target Server Type    : MYSQL
Target Server Version : 50621
File Encoding         : 65001

Date: 2017-10-21 13:48:40
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for deadline
-- ----------------------------
DROP TABLE IF EXISTS `deadline`;
CREATE TABLE `deadline` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `course_id` int(11) DEFAULT NULL,
  `start_day` varchar(100) DEFAULT NULL,
  `end_day` varchar(100) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of deadline
-- ----------------------------
INSERT INTO `deadline` VALUES ('1', '1', '2016-02-24', '2016-03-12', '迭代一');
INSERT INTO `deadline` VALUES ('2', '1', '2016-03-12', '2016-04-16', '迭代二');
INSERT INTO `deadline` VALUES ('3', '1', '2016-04-16', '2016-06-17', '迭代三');
