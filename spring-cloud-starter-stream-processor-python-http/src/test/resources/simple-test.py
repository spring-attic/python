def input():
    return "Pre" + payload;


def output():
    return payload + "Post";

result = locals()[channel]()
