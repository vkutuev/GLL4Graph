mkdir results
docker build -t iguana:latest .
docker run -it --rm iguana:latest bash