#! /bin/bash
#
# Wrapper script for myblockchain_config to support multilib
#
# Only works on OEL6/RHEL6 and similar
#
# This command respects setarch

bits=$(rpm --eval %__isa_bits)

case $bits in
    32|64) status=known ;;
        *) status=unknown ;;
esac

if [ "$status" = "unknown" ] ; then
    echo "$0: error: command 'rpm --eval %__isa_bits' returned unknown value: $bits"
    exit 1
fi


if [ -x /usr/bin/myblockchain_config-$bits ] ; then
    /usr/bin/myblockchain_config-$bits "$@"
else
    echo "$0: error: needed binary: /usr/bin/myblockchain_config-$bits is missing. Please check your MyBlockchain installation."
    exit 1
fi

