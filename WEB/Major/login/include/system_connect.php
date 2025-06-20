<?php
ob_start();
session_start();
putenv('TZ=Asia/Calcutta');
date_default_timezone_set("Asia/Calcutta");
$mysqli=new mysqli("localhost","thedeltas_usrmajor","}GF5;^7Ex(ja.Dw1","thedeltas_major");
$user_id=$_SESSION['user_id'];
?>