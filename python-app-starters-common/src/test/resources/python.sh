#This is a comment
export PATH=$HOME/.cloudfoundry/python/bin:$PATH
export PYTHONUNBUFFERED=true
export PYTHONHOME=/app/.cloudfoundry/python
export LIBRARY_PATH=/app/.cloudfoundry/vendor/lib:/app/.cloudfoundry/python/lib:$LIBRARY_PATH
export LD_LIBRARY_PATH=/app/.cloudfoundry/vendor/lib:/app/.cloudfoundry/python/lib:$LD_LIBRARY_PATH
export LANG=${LANG:-en_US.UTF-8}
export PYTHONHASHSEED=${PYTHONHASHSEED:-random}
export PYTHONPATH=${PYTHONPATH:-/app/}