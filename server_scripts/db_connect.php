<?php

  define('HOST', 'localhost');
  define('USER', 'getbetter');
  define('PASS', 'g3tb3tt3r');
  define('DB', 'get_better');

  // $con = mysqli_connect(HOST, USER, PASS, DB) or die ('unable to connect');

  $mysqli = new mysqli(HOST, USER, PASS, DB);

  if($mysqli->connect_errno) {
    printf("Connect Failed: %s\n", $mysqli->connect_errno);
  }



 ?>
